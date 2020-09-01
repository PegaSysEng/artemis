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

package tech.pegasys.teku.core;

import com.google.common.collect.Maps;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.bls.BLSKeyPair;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.core.lookup.BlockProvider;
import tech.pegasys.teku.core.lookup.StateAndBlockProvider;
import tech.pegasys.teku.core.signatures.MessageSignerService;
import tech.pegasys.teku.core.signatures.TestMessageSignerService;
import tech.pegasys.teku.datastructures.blocks.BeaconBlock;
import tech.pegasys.teku.datastructures.blocks.BeaconBlockBodyLists;
import tech.pegasys.teku.datastructures.blocks.Eth1Data;
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.datastructures.blocks.SignedBlockAndState;
import tech.pegasys.teku.datastructures.operations.Attestation;
import tech.pegasys.teku.datastructures.operations.DepositData;
import tech.pegasys.teku.datastructures.operations.ProposerSlashing;
import tech.pegasys.teku.datastructures.operations.SignedVoluntaryExit;
import tech.pegasys.teku.datastructures.state.BeaconState;
import tech.pegasys.teku.datastructures.state.Checkpoint;
import tech.pegasys.teku.datastructures.util.DepositGenerator;
import tech.pegasys.teku.datastructures.util.MockStartBeaconStateGenerator;
import tech.pegasys.teku.datastructures.util.MockStartDepositGenerator;
import tech.pegasys.teku.datastructures.util.MockStartValidatorKeyPairFactory;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;
import tech.pegasys.teku.ssz.SSZTypes.SSZList;
import tech.pegasys.teku.ssz.SSZTypes.SSZMutableList;
import tech.pegasys.teku.util.config.Constants;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.util.Preconditions.checkState;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.compute_epoch_at_slot;
import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.compute_start_slot_at_epoch;
import static tech.pegasys.teku.util.config.Constants.SLOTS_PER_EPOCH;

/** A utility for building small, valid chains of blocks with states for testing */
public class ChainBuilder {
  private static final List<BLSKeyPair> DEFAULT_VALIDATOR_KEYS =
      Collections.unmodifiableList(new MockStartValidatorKeyPairFactory().generateKeyPairs(0, 3));

  private final List<BLSKeyPair> validatorKeys;
  private final AttestationGenerator attestationGenerator;
  private final VoluntaryExitGenerator voluntaryExitGenerator;
  private final NavigableMap<UInt64, SignedBlockAndState> blocks = new TreeMap<>();
  private final Map<Bytes32, SignedBlockAndState> blocksByHash = new HashMap<>();

  private BlockProposalTestUtil blockProposalTestUtil = new BlockProposalTestUtil();

  private ChainBuilder(
      final List<BLSKeyPair> validatorKeys, final Map<UInt64, SignedBlockAndState> existingBlocks) {
    this.validatorKeys = validatorKeys;

    attestationGenerator = new AttestationGenerator(validatorKeys);
    voluntaryExitGenerator = new VoluntaryExitGenerator(validatorKeys);
    blocks.putAll(existingBlocks);
    existingBlocks.values().forEach(b -> blocksByHash.put(b.getRoot(), b));
  }

  public static ChainBuilder createDefault() {
    return ChainBuilder.create(DEFAULT_VALIDATOR_KEYS);
  }

  public static ChainBuilder create(final List<BLSKeyPair> validatorKeys) {
    return new ChainBuilder(validatorKeys, Collections.emptyMap());
  }

  public Optional<SignedBeaconBlock> getBlock(final Bytes32 blockRoot) {
    return Optional.ofNullable(blocksByHash.get(blockRoot)).map(SignedBlockAndState::getBlock);
  }

  public Optional<SignedBlockAndState> getBlockAndState(final Bytes32 blockRoot) {
    return Optional.ofNullable(blocksByHash.get(blockRoot));
  }

  public BlockProvider getBlockProvider() {
    return BlockProvider.fromDynamicMap(
        () -> Maps.transformValues(blocksByHash, SignedBlockAndState::getBlock));
  }

  public StateAndBlockProvider getStateAndBlockProvider() {
    return blockRoot -> SafeFuture.completedFuture(getBlockAndState(blockRoot));
  }
  /**
   * Create an independent {@code ChainBuilder} with the same history as the current builder. This
   * independent copy can now create a divergent chain.
   *
   * @return An independent copy of this ChainBuilder
   */
  public ChainBuilder fork() {
    return new ChainBuilder(validatorKeys, blocks);
  }

  public List<BLSKeyPair> getValidatorKeys() {
    return validatorKeys;
  }

  public UInt64 getLatestSlot() {
    assertChainIsNotEmpty();
    return getLatestBlockAndState().getBlock().getSlot();
  }

  public UInt64 getLatestEpoch() {
    assertChainIsNotEmpty();
    final UInt64 slot = getLatestSlot();
    return compute_epoch_at_slot(slot);
  }

  public Stream<SignedBlockAndState> streamBlocksAndStates() {
    return blocks.values().stream();
  }

  public Stream<SignedBlockAndState> streamBlocksAndStates(final long fromSlot, final long toSlot) {
    return streamBlocksAndStates(UInt64.valueOf(fromSlot), UInt64.valueOf(toSlot));
  }

  public Stream<SignedBlockAndState> streamBlocksAndStates(final long fromSlot) {
    return streamBlocksAndStates(UInt64.valueOf(fromSlot));
  }

  public Stream<SignedBlockAndState> streamBlocksAndStates(final UInt64 fromSlot) {
    return streamBlocksAndStates(fromSlot, getLatestSlot());
  }

  public Stream<SignedBlockAndState> streamBlocksAndStates(
      final UInt64 fromSlot, final UInt64 toSlot) {
    return blocks.values().stream()
        .filter(b -> b.getBlock().getSlot().compareTo(fromSlot) >= 0)
        .filter(b -> b.getBlock().getSlot().compareTo(toSlot) <= 0);
  }

  public Stream<SignedBlockAndState> streamBlocksAndStatesUpTo(final long toSlot) {
    return streamBlocksAndStatesUpTo(UInt64.valueOf(toSlot));
  }

  public Stream<SignedBlockAndState> streamBlocksAndStatesUpTo(final UInt64 toSlot) {
    return blocks.values().stream().filter(b -> b.getBlock().getSlot().compareTo(toSlot) <= 0);
  }

  public SignedBlockAndState getGenesis() {
    return Optional.ofNullable(blocks.firstEntry()).map(Map.Entry::getValue).orElse(null);
  }

  public SignedBlockAndState getLatestBlockAndState() {
    return Optional.ofNullable(blocks.lastEntry()).map(Map.Entry::getValue).orElse(null);
  }

  public SignedBlockAndState getBlockAndStateAtSlot(final long slot) {
    return getBlockAndStateAtSlot(UInt64.valueOf(slot));
  }

  public SignedBlockAndState getBlockAndStateAtSlot(final UInt64 slot) {
    return Optional.ofNullable(blocks.get(slot)).orElse(null);
  }

  public SignedBeaconBlock getBlockAtSlot(final long slot) {
    return getBlockAtSlot(UInt64.valueOf(slot));
  }

  public SignedBeaconBlock getBlockAtSlot(final UInt64 slot) {
    return resultToBlock(getBlockAndStateAtSlot(slot));
  }

  public BeaconState getStateAtSlot(final long slot) {
    return getStateAtSlot(UInt64.valueOf(slot));
  }

  public BeaconState getStateAtSlot(final UInt64 slot) {
    return resultToState(getBlockAndStateAtSlot(slot));
  }

  public SignedBlockAndState getLatestBlockAndStateAtSlot(final long slot) {
    return getLatestBlockAndStateAtSlot(UInt64.valueOf(slot));
  }

  public SignedBlockAndState getLatestBlockAndStateAtSlot(final UInt64 slot) {
    return Optional.ofNullable(blocks.floorEntry(slot)).map(Map.Entry::getValue).orElse(null);
  }

  public SignedBlockAndState getLatestBlockAndStateAtEpochBoundary(final long epoch) {
    return getLatestBlockAndStateAtEpochBoundary(UInt64.valueOf(epoch));
  }

  public SignedBlockAndState getLatestBlockAndStateAtEpochBoundary(final UInt64 epoch) {
    assertChainIsNotEmpty();
    final UInt64 slot = compute_start_slot_at_epoch(epoch);
    return getLatestBlockAndStateAtSlot(slot);
  }

  public Checkpoint getCurrentCheckpointForEpoch(final long epoch) {
    return getCurrentCheckpointForEpoch(UInt64.valueOf(epoch));
  }

  public Checkpoint getCurrentCheckpointForEpoch(final UInt64 epoch) {
    assertChainIsNotEmpty();
    final SignedBeaconBlock block = getLatestBlockAndStateAtEpochBoundary(epoch).getBlock();
    return new Checkpoint(epoch, block.getMessage().hash_tree_root());
  }

  public SignedBlockAndState generateGenesis() {
    return generateGenesis(true);
  }

  public SignedBlockAndState generateGenesis(final boolean signDeposits) {
    checkState(blocks.isEmpty(), "Genesis already created");

    // Generate genesis state
    final List<DepositData> initialDepositData =
        new MockStartDepositGenerator(new DepositGenerator(signDeposits))
            .createDeposits(validatorKeys);
    final BeaconState genesisState =
        new MockStartBeaconStateGenerator()
            .createInitialBeaconState(UInt64.ZERO, initialDepositData);

    // Generate genesis block
    BeaconBlock genesisBlock = new BeaconBlock(genesisState.hash_tree_root());
    final SignedBeaconBlock signedBlock = new SignedBeaconBlock(genesisBlock, BLSSignature.empty());

    final SignedBlockAndState blockAndState = new SignedBlockAndState(signedBlock, genesisState);
    trackBlock(blockAndState);
    return blockAndState;
  }

  public List<SignedBlockAndState> generateBlocksUpToSlot(final long slot) {
    return generateBlocksUpToSlot(UInt64.valueOf(slot));
  }

  public List<SignedBlockAndState> generateBlocksUpToSlot(final UInt64 slot) {
    assertBlockCanBeGenerated();
    final List<SignedBlockAndState> generated = new ArrayList<>();

    SignedBlockAndState latestBlock = getLatestBlockAndState();
    while (latestBlock.getState().getSlot().compareTo(slot) < 0) {
      latestBlock = generateNextBlock();
      generated.add(latestBlock);
    }

    return generated;
  }

  public SignedBlockAndState generateNextBlock() {
    assertBlockCanBeGenerated();
    return generateNextBlock(0);
  }

  public SignedBlockAndState generateNextBlock(final int skipSlots) {
    assertBlockCanBeGenerated();
    final SignedBlockAndState latest = getLatestBlockAndState();
    final UInt64 nextSlot = latest.getState().getSlot().plus(1 + skipSlots);
    return generateBlockAtSlot(nextSlot);
  }

  public SignedBlockAndState generateBlockAtSlot(final long slot) {
    return generateBlockAtSlot(UInt64.valueOf(slot));
  }

  public SignedBlockAndState generateBlockAtSlot(final long slot, final BlockOptions options) {
    return generateBlockAtSlot(UInt64.valueOf(slot));
  }

  public SignedBlockAndState generateBlockAtSlot(final UInt64 slot) {
    return generateBlockAtSlot(slot, BlockOptions.create());
  }

  public SignedBlockAndState generateBlockAtSlot(final UInt64 slot, final BlockOptions options) {
    assertBlockCanBeGenerated();
    final SignedBlockAndState latest = getLatestBlockAndState();
    checkState(
        slot.compareTo(latest.getState().getSlot()) > 0,
        "Cannot generate block at historical slot");

    return appendNewBlockToChain(slot, options);
  }

  /**
   * Utility for streaming valid attestations available for inclusion at the given slot. This
   * utility can be used to assign valid attestations to a generated block.
   *
   * @param slot The slot at which attestations are to be included
   * @return A stream of valid attestations that can be included in a block generated at the given
   *     slot
   */
  public Stream<Attestation> streamValidAttestationsForBlockAtSlot(final UInt64 slot) {
    // Calculate bounds for valid head blocks
    final UInt64 currentEpoch = compute_epoch_at_slot(slot);
    final UInt64 prevEpoch =
        currentEpoch.compareTo(UInt64.ZERO) == 0 ? currentEpoch : currentEpoch.minus(UInt64.ONE);
    final UInt64 minBlockSlot = compute_start_slot_at_epoch(prevEpoch);

    // Calculate valid assigned slots to be included in a block at the given slot
    final UInt64 slotsPerEpoch = UInt64.valueOf(SLOTS_PER_EPOCH);
    final UInt64 minAssignedSlot =
        slot.compareTo(slotsPerEpoch) <= 0 ? UInt64.ZERO : slot.minus(slotsPerEpoch);
    final UInt64 minInclusionDiff = UInt64.valueOf(Constants.MIN_ATTESTATION_INCLUSION_DELAY);
    final UInt64 maxAssignedSlot =
        slot.compareTo(minInclusionDiff) <= 0 ? slot : slot.minus(minInclusionDiff);

    // Generate stream of consistent, valid attestations for inclusion
    return LongStream.rangeClosed(minAssignedSlot.longValue(), maxAssignedSlot.longValue())
        .mapToObj(UInt64::valueOf)
        .map(this::getLatestBlockAndStateAtSlot)
        .filter(Objects::nonNull)
        .filter(b -> b.getSlot().compareTo(minBlockSlot) >= 0)
        .flatMap(this::streamValidAttestationsWithTargetBlock);
  }

  /**
   * Utility for streaming valid attestations with a specific target block.
   *
   * @param attestedHead the block to use as the attestation target
   * @return a stream of valid attestations voting for the specified block
   */
  public Stream<Attestation> streamValidAttestationsWithTargetBlock(
      final SignedBlockAndState attestedHead) {
    return attestationGenerator.streamAttestations(
        attestedHead.toUnsigned(), attestedHead.getSlot());
  }

  private void assertChainIsNotEmpty() {
    checkState(!blocks.isEmpty(), "Unable to execute operation on empty chain");
  }

  private void assertBlockCanBeGenerated() {
    checkState(!blocks.isEmpty(), "Genesis block must be created before blocks can be added.");
  }

  private void trackBlock(final SignedBlockAndState block) {
    blocks.put(block.getSlot(), block);
    blocksByHash.put(block.getRoot(), block);
  }

  private SignedBlockAndState appendNewBlockToChain(final UInt64 slot, final BlockOptions options) {
    final SignedBlockAndState latestBlockAndState = getLatestBlockAndState();
    final BeaconState preState = latestBlockAndState.getState();
    final Bytes32 parentRoot = latestBlockAndState.getBlock().getMessage().hash_tree_root();

    final int proposerIndex = blockProposalTestUtil.getProposerIndexForSlot(preState, slot);
    final MessageSignerService signer = getSigner(proposerIndex);
    final SignedBlockAndState nextBlockAndState;
    try {
      nextBlockAndState =
          blockProposalTestUtil.createBlock(
                  signer,
                  slot,
                  preState,
                  parentRoot,
                  Optional.of(options.getAttestations()),
                  Optional.of(options.getProposerSlashings()),
                  Optional.empty(),
                  Optional.of(options.getVoluntaryExits()),
                  options.getEth1Data());
      trackBlock(nextBlockAndState);
      return nextBlockAndState;
    } catch (StateTransitionException e) {
      throw new RuntimeException(e);
    }
  }

  private BeaconState resultToState(final SignedBlockAndState result) {
    return Optional.ofNullable(result).map(SignedBlockAndState::getState).orElse(null);
  }

  private SignedBeaconBlock resultToBlock(final SignedBlockAndState result) {
    return Optional.ofNullable(result).map(SignedBlockAndState::getBlock).orElse(null);
  }

  private MessageSignerService getSigner(final int proposerIndex) {
    return new TestMessageSignerService(validatorKeys.get(proposerIndex));
  }

  public static final class BlockOptions {
    private SSZMutableList<Attestation> attestations = BeaconBlockBodyLists.createAttestations();
    private SSZMutableList<ProposerSlashing> proposerSlashings = BeaconBlockBodyLists.createProposerSlashings();
    private SSZMutableList<SignedVoluntaryExit> voluntaryExits = BeaconBlockBodyLists.createVoluntaryExits();
    private Optional<Eth1Data> eth1Data = Optional.empty();

    private BlockOptions() {}

    public static BlockOptions create() {
      return new BlockOptions();
    }

    public BlockOptions addAttestation(final Attestation attestation) {
      attestations.add(attestation);
      return this;
    }

    public BlockOptions addProposerSlashings(final ProposerSlashing proposerSlashing) {
      proposerSlashings.add(proposerSlashing);
      return this;
    }

    public BlockOptions addVoluntaryExits(final SignedVoluntaryExit exit) {
      voluntaryExits.add(exit);
      return this;
    }

    public BlockOptions setEth1Data(final Eth1Data eth1Data) {
      this.eth1Data = Optional.ofNullable(eth1Data);
      return this;
    }

    private SSZMutableList<ProposerSlashing> getProposerSlashings() {
      return proposerSlashings;
    }

    private SSZMutableList<SignedVoluntaryExit> getVoluntaryExits() {
      return voluntaryExits;
    }

    private SSZList<Attestation> getAttestations() {
      return attestations;
    }

    public Optional<Eth1Data> getEth1Data() {
      return eth1Data;
    }
  }
}
