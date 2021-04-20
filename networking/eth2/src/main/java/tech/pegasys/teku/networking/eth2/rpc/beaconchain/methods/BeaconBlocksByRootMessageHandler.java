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

package tech.pegasys.teku.networking.eth2.rpc.beaconchain.methods;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.networking.eth2.peers.Eth2Peer;
import tech.pegasys.teku.networking.eth2.rpc.beaconchain.BeaconChainMethodIds;
import tech.pegasys.teku.networking.eth2.rpc.core.PeerRequiredLocalMessageHandler;
import tech.pegasys.teku.networking.eth2.rpc.core.ResponseCallback;
import tech.pegasys.teku.networking.eth2.rpc.core.RpcException;
import tech.pegasys.teku.networking.eth2.rpc.core.RpcException.InvalidRpcMethodVersion;
import tech.pegasys.teku.networking.p2p.peer.DisconnectReason;
import tech.pegasys.teku.spec.Spec;
import tech.pegasys.teku.spec.SpecMilestone;
import tech.pegasys.teku.spec.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.spec.datastructures.networking.libp2p.rpc.BeaconBlocksByRootRequestMessage;
import tech.pegasys.teku.ssz.primitive.SszBytes32;
import tech.pegasys.teku.storage.client.RecentChainData;

public class BeaconBlocksByRootMessageHandler
    extends PeerRequiredLocalMessageHandler<BeaconBlocksByRootRequestMessage, SignedBeaconBlock> {
  private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger();

  private final Spec spec;
  private final RecentChainData storageClient;

  public BeaconBlocksByRootMessageHandler(final Spec spec, final RecentChainData storageClient) {
    this.spec = spec;
    this.storageClient = storageClient;
  }

  @Override
  public void onIncomingMessage(
      final String protocolId,
      final Eth2Peer peer,
      final BeaconBlocksByRootRequestMessage message,
      final ResponseCallback<SignedBeaconBlock> callback) {
    LOG.trace("Peer {} requested BeaconBlocks with roots: {}", peer.getId(), message);
    if (storageClient.getStore() != null) {
      SafeFuture<Void> future = SafeFuture.COMPLETE;
      if (!peer.wantToMakeRequest() || !peer.wantToReceiveObjects(callback, message.size())) {
        peer.disconnectCleanly(DisconnectReason.RATE_LIMITING).reportExceptions();
        return;
      }

      for (SszBytes32 blockRoot : message) {
        future =
            future.thenCompose(
                __ ->
                    storageClient
                        .getStore()
                        .retrieveSignedBlock(blockRoot.get())
                        .thenCompose(
                            block -> {
                              final Optional<RpcException> validationResult =
                                  block.flatMap(b -> validateResponse(protocolId, b));
                              if (validationResult.isPresent()) {
                                callback.completeWithErrorResponse(validationResult.get());
                                return SafeFuture.failedFuture(validationResult.get());
                              }
                              return block.map(callback::respond).orElse(SafeFuture.COMPLETE);
                            }));
      }
      future.finish(callback::completeSuccessfully, callback::completeWithUnexpectedError);
    } else {
      callback.completeSuccessfully();
    }
  }

  @VisibleForTesting
  Optional<RpcException> validateResponse(
      final String protocolId, final SignedBeaconBlock response) {
    final int version = BeaconChainMethodIds.extractBeaconBlocksByRootVersion(protocolId);
    final Optional<UInt64> altairActivationEpoch =
        spec.getForkSchedule().getMilestoneActivationEpoch(SpecMilestone.ALTAIR);
    if (altairActivationEpoch.isEmpty()) {
      return Optional.empty();
    }

    final UInt64 altairActivationSlot = spec.computeStartSlotAtEpoch(altairActivationEpoch.get());
    final UInt64 responseSlot = response.getSlot();

    if (version == 1 && responseSlot.isGreaterThanOrEqualTo(altairActivationSlot)) {
      return Optional.of(
          new InvalidRpcMethodVersion("Must request altair blocks using v2 protocol"));
    }

    return Optional.empty();
  }
}
