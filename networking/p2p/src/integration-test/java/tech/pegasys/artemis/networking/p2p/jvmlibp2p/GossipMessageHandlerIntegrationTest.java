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

package tech.pegasys.artemis.networking.p2p.jvmlibp2p;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tech.pegasys.artemis.util.Waiter.ensureConditionRemainsMet;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.common.primitives.UnsignedLong;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import tech.pegasys.artemis.datastructures.blocks.BeaconBlock;
import tech.pegasys.artemis.datastructures.operations.Attestation;
import tech.pegasys.artemis.datastructures.util.MockStartValidatorKeyPairFactory;
import tech.pegasys.artemis.network.p2p.jvmlibp2p.NetworkFactory;
import tech.pegasys.artemis.networking.p2p.JvmLibP2PNetwork;
import tech.pegasys.artemis.statetransition.AttestationGenerator;
import tech.pegasys.artemis.statetransition.BeaconChainUtil;
import tech.pegasys.artemis.statetransition.events.CommitteeAssignmentEvent;
import tech.pegasys.artemis.statetransition.events.CommitteeDismissalEvent;
import tech.pegasys.artemis.storage.ChainStorageClient;
import tech.pegasys.artemis.util.Waiter;
import tech.pegasys.artemis.util.bls.BLSKeyPair;

public class GossipMessageHandlerIntegrationTest {

  private final NetworkFactory networkFactory = new NetworkFactory();

  @AfterEach
  public void tearDown() {
    networkFactory.stopAll();
  }

  @Test
  public void shouldGossipBlocksAcrossToIndirectlyConnectedPeers() throws Exception {
    // Setup network 1
    final EventBus eventBus1 = new EventBus();
    final ChainStorageClient storageClient1 = new ChainStorageClient(eventBus1);
    final JvmLibP2PNetwork network1 = networkFactory.startNetwork(eventBus1, storageClient1);
    final BeaconChainUtil chainUtil = BeaconChainUtil.create(12, storageClient1);
    chainUtil.initializeStorage();

    // Setup network 2
    final EventBus eventBus2 = new EventBus();
    final ChainStorageClient storageClient2 = new ChainStorageClient(eventBus2);
    final JvmLibP2PNetwork network2 = networkFactory.startNetwork(eventBus2, storageClient2);
    chainUtil.initializeStorage(storageClient2);

    // Setup network 3
    final EventBus eventBus3 = new EventBus();
    final ChainStorageClient storageClient3 = new ChainStorageClient(eventBus3);
    final JvmLibP2PNetwork network3 = networkFactory.startNetwork(eventBus3, storageClient3);
    chainUtil.initializeStorage(storageClient3);

    // Connect networks 1 -> 2 -> 3
    network1.connect(network2.getPeerAddress());
    network2.connect(network3.getPeerAddress());
    // Wait for connections to get set up
    Waiter.waitFor(
        () -> {
          assertThat(network1.getPeerManager().getAvailablePeerCount()).isEqualTo(1);
          assertThat(network2.getPeerManager().getAvailablePeerCount()).isEqualTo(2);
          assertThat(network3.getPeerManager().getAvailablePeerCount()).isEqualTo(1);
        });
    // TODO: debug this - we shouldn't have to wait here
    Thread.sleep(2000);

    // Propagate block from network 1
    final BeaconBlock newBlock = chainUtil.createBlockAtSlot(UnsignedLong.valueOf(2L));
    eventBus1.post(newBlock);

    // Listen for new block event to arrive on networks 2 and 3
    final BeaconBlockCollector network2Blocks = new BeaconBlockCollector(eventBus2);
    final BeaconBlockCollector network3Blocks = new BeaconBlockCollector(eventBus3);

    // Verify the expected block was gossiped across the network
    Waiter.waitFor(
        () -> {
          assertThat(network2Blocks.getBlocks()).containsExactly(newBlock);
          assertThat(network3Blocks.getBlocks()).containsExactly(newBlock);
        });
  }

  @Test
  public void shouldNotGossipAttestationsAcrossPeersThatAreNotOnTheSameSubnet() throws Exception {
    // Setup network 1
    final EventBus eventBus1 = new EventBus();
    final ChainStorageClient storageClient1 = new ChainStorageClient(eventBus1);
    final JvmLibP2PNetwork network1 = networkFactory.startNetwork(eventBus1, storageClient1);
    List<BLSKeyPair> blsKeyPairList =
        new MockStartValidatorKeyPairFactory().generateKeyPairs(0, 12);
    final BeaconChainUtil chainUtil = BeaconChainUtil.create(storageClient1, blsKeyPairList);
    chainUtil.initializeStorage();

    // Setup network 2
    final EventBus eventBus2 = new EventBus();
    final ChainStorageClient storageClient2 = new ChainStorageClient(eventBus2);
    final JvmLibP2PNetwork network2 = networkFactory.startNetwork(eventBus2, storageClient2);
    chainUtil.initializeStorage(storageClient2);

    // Connect networks 1 -> 2
    network1.connect(network2.getPeerAddress());
    // Wait for connections to get set up
    Waiter.waitFor(
        () -> {
          assertThat(network1.getPeerManager().getAvailablePeerCount()).isEqualTo(1);
          assertThat(network2.getPeerManager().getAvailablePeerCount()).isEqualTo(1);
        });

    // Listen for new attestation event to arrive on network 2
    final AttestationCollector network2Attestations = new AttestationCollector(eventBus2);

    // Propagate attestation from network 1
    AttestationGenerator attestationGenerator = new AttestationGenerator(blsKeyPairList);
    Attestation validAttestation = attestationGenerator.validAttestation(storageClient1);
    eventBus1.post(validAttestation);

    ensureConditionRemainsMet(() -> assertThat(network2Attestations.getAttestations()).isEmpty());
  }

  @Test
  public void shouldGossipAttestationsAcrossPeersThatAreOnTheSameSubnet() throws Exception {
    // Setup network 1
    final EventBus eventBus1 = new EventBus();
    final ChainStorageClient storageClient1 = new ChainStorageClient(eventBus1);
    final JvmLibP2PNetwork network1 = networkFactory.startNetwork(eventBus1, storageClient1);
    List<BLSKeyPair> blsKeyPairList =
        new MockStartValidatorKeyPairFactory().generateKeyPairs(0, 12);
    final BeaconChainUtil chainUtil = BeaconChainUtil.create(storageClient1, blsKeyPairList);
    chainUtil.initializeStorage();

    // Setup network 2
    final EventBus eventBus2 = new EventBus();
    final ChainStorageClient storageClient2 = new ChainStorageClient(eventBus2);
    final JvmLibP2PNetwork network2 = networkFactory.startNetwork(eventBus2, storageClient2);
    chainUtil.initializeStorage(storageClient2);

    // Connect networks 1 -> 2
    network1.connect(network2.getPeerAddress());
    // Wait for connections to get set up
    Waiter.waitFor(
        () -> {
          assertThat(network1.getPeerManager().getAvailablePeerCount()).isEqualTo(1);
          assertThat(network2.getPeerManager().getAvailablePeerCount()).isEqualTo(1);
        });

    // Listen for new attestation event to arrive on network 2
    final AttestationCollector network2Attestations = new AttestationCollector(eventBus2);

    // Propagate attestation from network 1
    AttestationGenerator attestationGenerator = new AttestationGenerator(blsKeyPairList);
    Attestation validAttestation = attestationGenerator.validAttestation(storageClient1);

    eventBus1.post(
        new CommitteeAssignmentEvent(List.of(validAttestation.getData().getIndex().intValue())));
    eventBus2.post(
        new CommitteeAssignmentEvent(List.of(validAttestation.getData().getIndex().intValue())));

    // Sleep here because registering to new topics take time
    Thread.sleep(1000);

    eventBus1.post(validAttestation);

    Waiter.waitFor(
        () -> assertThat(network2Attestations.getAttestations()).containsExactly(validAttestation));
  }

  @Test
  public void shouldNotGossipAttestationsWhenPeerDeregistersFromTopic() throws Exception {
    // Setup network 1
    final EventBus eventBus1 = new EventBus();
    final ChainStorageClient storageClient1 = new ChainStorageClient(eventBus1);
    final JvmLibP2PNetwork network1 = networkFactory.startNetwork(eventBus1, storageClient1);
    List<BLSKeyPair> blsKeyPairList =
        new MockStartValidatorKeyPairFactory().generateKeyPairs(0, 12);
    final BeaconChainUtil chainUtil = BeaconChainUtil.create(storageClient1, blsKeyPairList);
    chainUtil.initializeStorage();

    // Setup network 2
    final EventBus eventBus2 = new EventBus();
    final ChainStorageClient storageClient2 = new ChainStorageClient(eventBus2);
    final JvmLibP2PNetwork network2 = networkFactory.startNetwork(eventBus2, storageClient2);
    chainUtil.initializeStorage(storageClient2);

    // Connect networks 1 -> 2
    network1.connect(network2.getPeerAddress());
    // Wait for connections to get set up
    Waiter.waitFor(
        () -> {
          assertThat(network1.getPeerManager().getAvailablePeerCount()).isEqualTo(1);
          assertThat(network2.getPeerManager().getAvailablePeerCount()).isEqualTo(1);
        });

    // Listen for new attestation event to arrive on network 2
    final AttestationCollector network2Attestations = new AttestationCollector(eventBus2);

    // Propagate attestation from network 1
    AttestationGenerator attestationGenerator = new AttestationGenerator(blsKeyPairList);
    Attestation validAttestation = attestationGenerator.validAttestation(storageClient1);

    eventBus1.post(
        new CommitteeAssignmentEvent(List.of(validAttestation.getData().getIndex().intValue())));
    eventBus2.post(
        new CommitteeAssignmentEvent(List.of(validAttestation.getData().getIndex().intValue())));
    // Sleep here because registering to new topics take time
    Thread.sleep(1000);
    eventBus1.post(validAttestation);

    Thread.sleep(1000);
    // Sleep here because receiving new messages over the network takes time
    assertTrue(network2Attestations.getAttestations().contains(validAttestation));

    eventBus1.post(
        new CommitteeDismissalEvent(List.of(validAttestation.getData().getIndex().intValue())));
    eventBus2.post(
        new CommitteeDismissalEvent(List.of(validAttestation.getData().getIndex().intValue())));

    // Sleep here because deregistering from topics take time
    Thread.sleep(1000);

    // Listen if the new attestation arrives on network 2
    final AttestationCollector network2AttestationsAfterDeregistration =
        new AttestationCollector(eventBus2);

    ensureConditionRemainsMet(
        () -> assertThat(network2AttestationsAfterDeregistration.getAttestations()).isEmpty());
  }

  private static class BeaconBlockCollector {
    private final Collection<BeaconBlock> blocks = new ConcurrentLinkedQueue<>();

    public BeaconBlockCollector(final EventBus eventBus) {
      eventBus.register(this);
    }

    @Subscribe
    public void onBeaconBlock(final BeaconBlock block) {
      blocks.add(block);
    }

    public Collection<BeaconBlock> getBlocks() {
      return blocks;
    }
  }

  private static class AttestationCollector {
    private final Collection<Attestation> attestations = new ConcurrentLinkedQueue<>();

    public AttestationCollector(final EventBus eventBus) {
      eventBus.register(this);
    }

    @Subscribe
    public void onAttestation(final Attestation attestation) {
      attestations.add(attestation);
    }

    public Collection<Attestation> getAttestations() {
      return attestations;
    }
  }
}
