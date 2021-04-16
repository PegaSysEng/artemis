/*
 * Copyright 2021 ConsenSys AG.
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

package tech.pegasys.teku.networking.eth2.rpc.core.encodings.context;

import java.util.Optional;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.networking.eth2.rpc.core.encodings.RpcByteBufDecoder;
import tech.pegasys.teku.spec.Spec;
import tech.pegasys.teku.spec.SpecMilestone;
import tech.pegasys.teku.spec.SpecVersion;
import tech.pegasys.teku.ssz.SszData;
import tech.pegasys.teku.ssz.schema.SszSchema;
import tech.pegasys.teku.ssz.type.Bytes4;
import tech.pegasys.teku.storage.client.RecentChainData;

abstract class ForkDigestRpcContextEncoder<TPayload extends SszData>
    implements RpcContextEncoder<Bytes4, TPayload> {
  private final Spec spec;
  private final RecentChainData recentChainData;
  private final ForkDigestPayloadContext<TPayload> payloadContext;

  protected ForkDigestRpcContextEncoder(
      final Spec spec,
      final RecentChainData recentChainData,
      final ForkDigestPayloadContext<TPayload> payloadContext) {
    this.spec = spec;
    this.recentChainData = recentChainData;
    this.payloadContext = payloadContext;
  }

  @Override
  public RpcByteBufDecoder<Bytes4> getContextDecoder() {
    return new ForkDigestContextDecoder();
  }

  @Override
  public Bytes4 encodeContext(TPayload responsePayload) {
    final UInt64 slot = payloadContext.getSlotFromPayload(responsePayload);
    final SpecMilestone specMilestone = spec.getForkSchedule().getSpecMilestoneAtSlot(slot);
    return recentChainData.getForkDigestByMilestone(specMilestone).orElseThrow();
  }

  @Override
  public Optional<SszSchema<TPayload>> getSchemaFromContext(final Bytes4 forkDigest) {
    return recentChainData
        .getMilestoneByForkDigest(forkDigest)
        .map(spec::forMilestone)
        .map(SpecVersion::getSchemaDefinitions)
        .map(payloadContext::getSchemaFromSchemaDefinitions);
  }
}
