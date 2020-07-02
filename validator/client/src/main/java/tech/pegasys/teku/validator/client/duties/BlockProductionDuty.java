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

package tech.pegasys.teku.validator.client.duties;

import static tech.pegasys.teku.datastructures.util.BeaconStateUtil.compute_epoch_at_slot;

import com.google.common.primitives.UnsignedLong;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.teku.bls.BLSSignature;
import tech.pegasys.teku.datastructures.blocks.BeaconBlock;
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.datastructures.state.ForkInfo;
import tech.pegasys.teku.util.async.SafeFuture;
import tech.pegasys.teku.validator.api.ValidatorApiChannel;
import tech.pegasys.teku.validator.client.ForkProvider;
import tech.pegasys.teku.validator.client.Validator;

public class BlockProductionDuty implements Duty {
  private static final Logger LOG = LogManager.getLogger();
  private final Validator validator;
  private final UnsignedLong slot;
  private final ForkProvider forkProvider;
  private final ValidatorApiChannel validatorApiChannel;

  public BlockProductionDuty(
      final Validator validator,
      final UnsignedLong slot,
      final ForkProvider forkProvider,
      final ValidatorApiChannel validatorApiChannel) {
    this.validator = validator;
    this.slot = slot;
    this.forkProvider = forkProvider;
    this.validatorApiChannel = validatorApiChannel;
  }

  @Override
  public SafeFuture<DutyResult> performDuty() {
    LOG.trace("Creating block for validator {} at slot {}", validator.getPublicKey(), slot);
    return forkProvider.getForkInfo().thenCompose(this::produceBlock);
  }

  @Override
  public String getProducedType() {
    return "block";
  }

  public SafeFuture<DutyResult> produceBlock(final ForkInfo forkInfo) {
    return createRandaoReveal(forkInfo)
        .thenCompose(this::createUnsignedBlock)
        .thenCompose(unsignedBlock -> signBlock(forkInfo, unsignedBlock))
        .thenApply(
            signedBlock -> {
              validatorApiChannel.sendSignedBlock(signedBlock);
              return DutyResult.success(signedBlock.getRoot());
            })
        .exceptionally(DutyResult::forError);
  }

  public SafeFuture<Optional<BeaconBlock>> createUnsignedBlock(final BLSSignature randaoReveal) {
    return validatorApiChannel.createUnsignedBlock(slot, randaoReveal, validator.getGraffiti());
  }

  public SafeFuture<BLSSignature> createRandaoReveal(final ForkInfo forkInfo) {
    return validator.getSigner().createRandaoReveal(compute_epoch_at_slot(slot), forkInfo);
  }

  public SafeFuture<SignedBeaconBlock> signBlock(
      final ForkInfo forkInfo, final Optional<BeaconBlock> unsignedBlock) {
    return validator
        .getSigner()
        .signBlock(
            unsignedBlock.orElseThrow(
                () -> new IllegalStateException("Node was not syncing but could not create block")),
            forkInfo)
        .thenApply(signature -> new SignedBeaconBlock(unsignedBlock.orElseThrow(), signature));
  }

  @Override
  public String toString() {
    return "BlockProductionDuty{"
        + "validator="
        + validator
        + ", slot="
        + slot
        + ", forkProvider="
        + forkProvider
        + '}';
  }
}
