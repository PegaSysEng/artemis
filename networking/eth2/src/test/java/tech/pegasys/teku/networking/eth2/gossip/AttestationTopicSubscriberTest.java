/*
 * Copyright 2020 ConsenSys AG.
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

package tech.pegasys.teku.networking.eth2.gossip;

import static com.google.common.primitives.UnsignedLong.ONE;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.primitives.UnsignedLong;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.networking.eth2.Eth2Network;

class AttestationTopicSubscriberTest {

  private final Eth2Network eth2Network = mock(Eth2Network.class);

  private final AttestationTopicSubscriber subscriber = new AttestationTopicSubscriber(eth2Network);

  @Test
  public void shouldSubscribeToSubnet() {
    final int subnetId = 10;
    subscriber.subscribeToCommitteeForAggregation(subnetId, ONE);

    verify(eth2Network).subscribeToAttestationSubnetId(subnetId);
  }

  @Test
  public void shouldUnsubscribeFromSubnetWhenPastSlot() {
    final int subnetId = 12;
    final UnsignedLong aggregationSlot = UnsignedLong.valueOf(10);

    subscriber.subscribeToCommitteeForAggregation(subnetId, aggregationSlot);
    subscriber.onSlot(aggregationSlot.plus(ONE));

    verify(eth2Network).unsubscribeFromAttestationSubnetId(subnetId);
  }

  @Test
  public void shouldNotUnsubscribeAtStartOfTargetSlot() {
    final int subnetId = 16;
    final UnsignedLong aggregationSlot = UnsignedLong.valueOf(10);

    subscriber.subscribeToCommitteeForAggregation(subnetId, aggregationSlot);
    subscriber.onSlot(aggregationSlot);

    verify(eth2Network, never()).unsubscribeFromAttestationSubnetId(subnetId);
  }

  @Test
  public void shouldExtendSubscriptionPeriod() {
    final int subnetId = 3;
    final UnsignedLong firstSlot = UnsignedLong.valueOf(10);
    final UnsignedLong secondSlot = UnsignedLong.valueOf(15);

    subscriber.subscribeToCommitteeForAggregation(subnetId, firstSlot);
    subscriber.subscribeToCommitteeForAggregation(subnetId, secondSlot);

    subscriber.onSlot(firstSlot.plus(ONE));
    verify(eth2Network, never()).unsubscribeFromAttestationSubnetId(subnetId);

    subscriber.onSlot(secondSlot.plus(ONE));
    verify(eth2Network).unsubscribeFromAttestationSubnetId(subnetId);
  }

  @Test
  public void shouldPreserveLaterSubscriptionPeriodWhenEarlierSlotAdded() {
    final int subnetId = 3;
    final UnsignedLong firstSlot = UnsignedLong.valueOf(10);
    final UnsignedLong secondSlot = UnsignedLong.valueOf(15);

    subscriber.subscribeToCommitteeForAggregation(subnetId, secondSlot);
    subscriber.subscribeToCommitteeForAggregation(subnetId, firstSlot);

    subscriber.onSlot(firstSlot.plus(ONE));
    verify(eth2Network, never()).unsubscribeFromAttestationSubnetId(subnetId);

    subscriber.onSlot(secondSlot.plus(ONE));
    verify(eth2Network).unsubscribeFromAttestationSubnetId(subnetId);
  }

  @Test
  public void shouldSubscribeToNewSubnetsAndUpdateENR_forRandomsSubscriptions() {
    Map<Integer, UnsignedLong> randomSubnetSubscriptions =
        Map.of(
            1, UnsignedLong.valueOf(20),
            2, UnsignedLong.valueOf(15));
    subscriber.subscribeToPersistentSubnets(randomSubnetSubscriptions);
    verify(eth2Network)
        .setLongTermAttestationSubnetSubscriptions(randomSubnetSubscriptions.keySet());

    verify(eth2Network).subscribeToAttestationSubnetId(1);
    verify(eth2Network).subscribeToAttestationSubnetId(2);
  }

  @Test
  public void shouldExtendSubscriptionPeriod_forRandomSubscriptions() {
    final int subnetId = 3;
    final UnsignedLong firstSlot = UnsignedLong.valueOf(10);
    final UnsignedLong secondSlot = UnsignedLong.valueOf(15);
    Map<Integer, UnsignedLong> randomSubnetSubscriptions = Map.of(subnetId, secondSlot);

    subscriber.subscribeToCommitteeForAggregation(subnetId, firstSlot);
    subscriber.subscribeToPersistentSubnets(randomSubnetSubscriptions);

    subscriber.onSlot(firstSlot.plus(ONE));
    verify(eth2Network, never()).unsubscribeFromAttestationSubnetId(subnetId);

    subscriber.onSlot(secondSlot.plus(ONE));
    verify(eth2Network).unsubscribeFromAttestationSubnetId(subnetId);
  }

  @Test
  public void shouldPreserveLaterSubscription_forRandomSubscriptions() {
    final int subnetId = 3;
    final UnsignedLong firstSlot = UnsignedLong.valueOf(10);
    final UnsignedLong secondSlot = UnsignedLong.valueOf(15);
    Map<Integer, UnsignedLong> randomSubnetSubscriptions = Map.of(subnetId, firstSlot);
    subscriber.subscribeToCommitteeForAggregation(subnetId, secondSlot);
    subscriber.subscribeToPersistentSubnets(randomSubnetSubscriptions);

    subscriber.onSlot(firstSlot.plus(ONE));
    verify(eth2Network, never()).unsubscribeFromAttestationSubnetId(subnetId);

    subscriber.onSlot(secondSlot.plus(ONE));
    verify(eth2Network).unsubscribeFromAttestationSubnetId(subnetId);
  }
}
