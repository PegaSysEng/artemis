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

package tech.pegasys.teku.spec.schemas.genesis;

import java.util.List;
import tech.pegasys.teku.datastructures.blocks.BeaconBlockHeader;
import tech.pegasys.teku.datastructures.blocks.Eth1Data;
import tech.pegasys.teku.datastructures.state.Checkpoint;
import tech.pegasys.teku.datastructures.state.Fork;
import tech.pegasys.teku.datastructures.state.PendingAttestation;
import tech.pegasys.teku.datastructures.state.Validator;
import tech.pegasys.teku.spec.constants.SpecConstants;
import tech.pegasys.teku.spec.containers.state.BeaconState;
import tech.pegasys.teku.spec.containers.state.BeaconStateFields;
import tech.pegasys.teku.spec.containers.state.BeaconStateSchema;
import tech.pegasys.teku.spec.schemas.SchemaDefinitions;
import tech.pegasys.teku.ssz.backing.schema.SszComplexSchemas;
import tech.pegasys.teku.ssz.backing.schema.SszContainerSchema;
import tech.pegasys.teku.ssz.backing.schema.SszListSchema;
import tech.pegasys.teku.ssz.backing.schema.SszPrimitiveSchemas;
import tech.pegasys.teku.ssz.backing.schema.SszSchemaHints;
import tech.pegasys.teku.ssz.backing.schema.SszVectorSchema;
import tech.pegasys.teku.ssz.sos.SszField;
import tech.pegasys.teku.util.config.Constants;

public class SchemaDefinitionsGenesis implements SchemaDefinitions {
  private final SpecConstants specConstants;

  public SchemaDefinitionsGenesis(final SpecConstants specConstants) {
    this.specConstants = specConstants;
  }

  @Override
  public SszContainerSchema<BeaconState> getBeaconStateSchema() {
    // TODO - use specConstants throughout rather than deprecated CONSTANTS
    // TODO - add methods to retrieve other schemas to ContainerDefinitions
    SszField genesis_time_field =
        new SszField(0, BeaconStateFields.GENESIS_TIME.name(), SszPrimitiveSchemas.UINT64_SCHEMA);
    SszField genesis_validators_root_field =
        new SszField(
            1,
            BeaconStateFields.GENESIS_VALIDATORS_ROOT.name(),
            SszPrimitiveSchemas.BYTES32_SCHEMA);
    SszField slot_field =
        new SszField(2, BeaconStateFields.SLOT.name(), SszPrimitiveSchemas.UINT64_SCHEMA);
    SszField fork_field = new SszField(3, BeaconStateFields.FORK.name(), Fork.SSZ_SCHEMA);
    SszField latest_block_header_field =
        new SszField(4, BeaconStateFields.LATEST_BLOCK_HEADER.name(), BeaconBlockHeader.SSZ_SCHEMA);
    SszField block_roots_field =
        new SszField(
            5,
            BeaconStateFields.BLOCK_ROOTS.name(),
            () ->
                new SszVectorSchema<>(
                    SszPrimitiveSchemas.BYTES32_SCHEMA, specConstants.getSlotsPerHistoricalRoot()));
    SszField state_roots_field =
        new SszField(
            6,
            BeaconStateFields.STATE_ROOTS.name(),
            () ->
                new SszVectorSchema<>(
                    SszPrimitiveSchemas.BYTES32_SCHEMA, specConstants.getSlotsPerHistoricalRoot()));
    SszField historical_roots_field =
        new SszField(
            7,
            BeaconStateFields.HISTORICAL_ROOTS.name(),
            () ->
                new SszListSchema<>(
                    SszPrimitiveSchemas.BYTES32_SCHEMA, specConstants.getHistoricalRootsLimit()));
    SszField eth1_data_field =
        new SszField(8, BeaconStateFields.ETH1_DATA.name(), Eth1Data.SSZ_SCHEMA);
    SszField eth1_data_votes_field =
        new SszField(
            9,
            BeaconStateFields.ETH1_DATA_VOTES.name(),
            () ->
                new SszListSchema<>(
                    Eth1Data.SSZ_SCHEMA,
                    Constants.EPOCHS_PER_ETH1_VOTING_PERIOD * Constants.SLOTS_PER_EPOCH));
    SszField eth1_deposit_index_field =
        new SszField(
            10, BeaconStateFields.ETH1_DEPOSIT_INDEX.name(), SszPrimitiveSchemas.UINT64_SCHEMA);
    SszField validators_field =
        new SszField(
            11,
            BeaconStateFields.VALIDATORS.name(),
            () ->
                new SszListSchema<>(
                    Validator.SSZ_SCHEMA,
                    Constants.VALIDATOR_REGISTRY_LIMIT,
                    SszSchemaHints.sszSuperNode(8)));
    SszField balances_field =
        new SszField(
            12,
            BeaconStateFields.BALANCES.name(),
            () ->
                new SszListSchema<>(
                    SszPrimitiveSchemas.UINT64_SCHEMA, Constants.VALIDATOR_REGISTRY_LIMIT));
    SszField randao_mixes_field =
        new SszField(
            13,
            BeaconStateFields.RANDAO_MIXES.name(),
            () ->
                new SszVectorSchema<>(
                    SszPrimitiveSchemas.BYTES32_SCHEMA, Constants.EPOCHS_PER_HISTORICAL_VECTOR));
    SszField slashings_field =
        new SszField(
            14,
            BeaconStateFields.SLASHINGS.name(),
            () ->
                new SszVectorSchema<>(
                    SszPrimitiveSchemas.UINT64_SCHEMA, Constants.EPOCHS_PER_SLASHINGS_VECTOR));
    SszField previous_epoch_attestations_field =
        new SszField(
            15,
            BeaconStateFields.PREVIOUS_EPOCH_ATTESTATIONS.name(),
            () ->
                new SszListSchema<>(
                    PendingAttestation.SSZ_SCHEMA,
                    Constants.MAX_ATTESTATIONS * Constants.SLOTS_PER_EPOCH));
    SszField current_epoch_attestations_field =
        new SszField(
            16,
            BeaconStateFields.CURRENT_EPOCH_ATTESTATIONS.name(),
            () ->
                new SszListSchema<>(
                    PendingAttestation.SSZ_SCHEMA,
                    Constants.MAX_ATTESTATIONS * Constants.SLOTS_PER_EPOCH));
    SszField justification_bits_field =
        new SszField(
            17,
            BeaconStateFields.JUSTIFICATION_BITS.name(),
            () -> new SszComplexSchemas.SszBitVectorSchema(Constants.JUSTIFICATION_BITS_LENGTH));
    SszField previous_justified_checkpoint_field =
        new SszField(
            18, BeaconStateFields.PREVIOUS_JUSTIFIED_CHECKPOINT.name(), Checkpoint.SSZ_SCHEMA);
    SszField current_justified_checkpoint_field =
        new SszField(
            19, BeaconStateFields.CURRENT_JUSTIFIED_CHECKPOINT.name(), Checkpoint.SSZ_SCHEMA);
    SszField finalized_checkpoint_field =
        new SszField(20, BeaconStateFields.FINALIZED_CHECKPOINT.name(), Checkpoint.SSZ_SCHEMA);

    return BeaconStateSchema.create(
        List.of(
            genesis_time_field,
            genesis_validators_root_field,
            slot_field,
            fork_field,
            latest_block_header_field,
            block_roots_field,
            state_roots_field,
            historical_roots_field,
            eth1_data_field,
            eth1_data_votes_field,
            eth1_deposit_index_field,
            validators_field,
            balances_field,
            randao_mixes_field,
            slashings_field,
            previous_epoch_attestations_field,
            current_epoch_attestations_field,
            justification_bits_field,
            previous_justified_checkpoint_field,
            current_justified_checkpoint_field,
            finalized_checkpoint_field));
  }
}
