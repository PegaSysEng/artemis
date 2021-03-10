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

package tech.pegasys.teku.spec.logic.versions.phase0;

import tech.pegasys.teku.spec.constants.SpecConstants;
import tech.pegasys.teku.spec.logic.common.AbstractSpecLogic;
import tech.pegasys.teku.spec.logic.common.statetransition.StateTransition;
import tech.pegasys.teku.spec.logic.common.statetransition.epoch.EpochProcessor;
import tech.pegasys.teku.spec.logic.common.statetransition.epoch.status.ValidatorStatusFactory;
import tech.pegasys.teku.spec.logic.common.util.AttestationUtil;
import tech.pegasys.teku.spec.logic.common.util.BeaconStateUtil;
import tech.pegasys.teku.spec.logic.common.util.BlockProcessorUtil;
import tech.pegasys.teku.spec.logic.common.util.BlockProposalUtil;
import tech.pegasys.teku.spec.logic.common.util.CommitteeUtil;
import tech.pegasys.teku.spec.logic.common.util.ForkChoiceUtil;
import tech.pegasys.teku.spec.logic.common.util.ValidatorsUtil;
import tech.pegasys.teku.spec.logic.versions.phase0.statetransition.epoch.EpochProcessorPhase0;
import tech.pegasys.teku.spec.logic.versions.phase0.statetransition.epoch.ValidatorStatusFactoryPhase0;
import tech.pegasys.teku.spec.logic.versions.phase0.util.BlockProcessorPhase0;
import tech.pegasys.teku.spec.schemas.SchemaDefinitions;

public class SpecLogicPhase0 extends AbstractSpecLogic {

  private SpecLogicPhase0(
      final CommitteeUtil committeeUtil,
      final ValidatorsUtil validatorsUtil,
      final BeaconStateUtil beaconStateUtil,
      final AttestationUtil attestationUtil,
      final ValidatorStatusFactory validatorStatusFactory,
      final EpochProcessor epochProcessor,
      final BlockProcessorUtil blockProcessorUtil,
      final StateTransition stateTransition,
      final ForkChoiceUtil forkChoiceUtil,
      final BlockProposalUtil blockProposalUtil) {
    super(
        committeeUtil,
        validatorsUtil,
        beaconStateUtil,
        attestationUtil,
        validatorStatusFactory,
        epochProcessor,
        blockProcessorUtil,
        stateTransition,
        forkChoiceUtil,
        blockProposalUtil);
  }

  public static SpecLogicPhase0 create(
      final SpecConstants constants, final SchemaDefinitions schemaDefinitions) {
    final CommitteeUtil committeeUtil = new CommitteeUtil(constants);
    final ValidatorsUtil validatorsUtil = new ValidatorsUtil(constants);
    final BeaconStateUtil beaconStateUtil =
        new BeaconStateUtil(constants, schemaDefinitions, validatorsUtil, committeeUtil);
    final AttestationUtil attestationUtil =
        new AttestationUtil(constants, beaconStateUtil, validatorsUtil);
    final ValidatorStatusFactory validatorStatusFactory =
        new ValidatorStatusFactoryPhase0(beaconStateUtil, attestationUtil, validatorsUtil);
    final EpochProcessor epochProcessor =
        new EpochProcessorPhase0(
            constants, validatorsUtil, beaconStateUtil, validatorStatusFactory);
    final BlockProcessorUtil blockProcessorUtil =
        new BlockProcessorPhase0(constants, beaconStateUtil, attestationUtil, validatorsUtil);
    final StateTransition stateTransition =
        StateTransition.create(
            constants, blockProcessorUtil, epochProcessor, beaconStateUtil, validatorsUtil);
    final ForkChoiceUtil forkChoiceUtil =
        new ForkChoiceUtil(constants, beaconStateUtil, attestationUtil, stateTransition);
    final BlockProposalUtil blockProposalUtil = new BlockProposalUtil(schemaDefinitions, stateTransition);

    return new SpecLogicPhase0(
        committeeUtil,
        validatorsUtil,
        beaconStateUtil,
        attestationUtil,
        validatorStatusFactory,
        epochProcessor,
        blockProcessorUtil,
        stateTransition,
        forkChoiceUtil,
        blockProposalUtil);
  }
}
