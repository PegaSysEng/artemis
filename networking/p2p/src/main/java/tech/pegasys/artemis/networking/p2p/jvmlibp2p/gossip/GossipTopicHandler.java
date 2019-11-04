/*
 * Copyright 2019 ConsenSys AG.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package tech.pegasys.artemis.networking.p2p.jvmlibp2p.gossip;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.eventbus.EventBus;
import io.libp2p.core.pubsub.MessageApi;
import io.libp2p.core.pubsub.PubsubPublisherApi;
import io.libp2p.core.pubsub.Topic;
import io.netty.buffer.Unpooled;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZException;
import tech.pegasys.artemis.datastructures.util.SimpleOffsetSerializer;
import tech.pegasys.artemis.util.collections.LimitedSet;
import tech.pegasys.artemis.util.collections.LimitedSet.Mode;
import tech.pegasys.artemis.util.sos.SimpleOffsetSerializable;

public abstract class GossipTopicHandler<T extends SimpleOffsetSerializable>
    implements Consumer<MessageApi> {
  private static final Logger LOG = LogManager.getLogger();
  private static final int MAX_SENT_MESSAGES = 2048;

  private final PubsubPublisherApi publisher;
  private final EventBus eventBus;

  private final Set<Bytes> processedMessages =
      LimitedSet.create(MAX_SENT_MESSAGES, Mode.DROP_LEAST_RECENTLY_ACCESSED);

  protected GossipTopicHandler(final PubsubPublisherApi publisher, final EventBus eventBus) {
    this.publisher = publisher;
    this.eventBus = eventBus;
  }

  public abstract Topic getTopic();

  @Override
  public final void accept(MessageApi message) {
    Bytes bytes = Bytes.wrapByteBuf(message.getData());
    if (!processedMessages.add(bytes)) {
      // We've already seen this message, skip processing
      LOG.trace("Ignoring duplicate message for topic {}: {} bytes", getTopic(), bytes.size());
      return;
    } else {
      LOG.trace("Received message for topic {}: {} bytes", getTopic(), bytes.size());
    }

    Optional<T> data;
    try {
      data = processData(message, bytes);
    } catch (SSZException e) {
      LOG.trace("Received malformed gossip message on {}", getTopic());
      data = Optional.empty();
    }

    // Post and re-gossip data on successful processing
    data.ifPresent((__) -> gossip(bytes));
    data.ifPresent(eventBus::post);
  }

  protected abstract Optional<T> processData(MessageApi message, Bytes bytes) throws SSZException;

  @VisibleForTesting
  public final void gossip(final T data) {
    final Bytes bytes = SimpleOffsetSerializer.serialize(data);
    if (!processedMessages.add(bytes)) {
      // We've already gossiped this data
      return;
    }
    gossip(bytes);
  }

  private void gossip(Bytes bytes) {
    LOG.trace("Gossiping {}: {} bytes", getTopic(), bytes.size());
    publisher.publish(Unpooled.wrappedBuffer(bytes.toArrayUnsafe()), getTopic());
  }
}
