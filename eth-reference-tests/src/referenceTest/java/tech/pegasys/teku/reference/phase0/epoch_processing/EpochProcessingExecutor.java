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

package tech.pegasys.teku.reference.phase0.epoch_processing;

import tech.pegasys.teku.spec.datastructures.state.beaconstate.MutableBeaconState;
import tech.pegasys.teku.spec.logic.common.statetransition.exceptions.EpochProcessingException;

public interface EpochProcessingExecutor {
  enum Operation {
    PROCESS_SLASHINGS,
    PROCESS_REGISTRY_UPDATES,
    PROCESS_REWARDS_AND_PENALTIES,
    PROCESS_JUSTIFICATION_AND_FINALIZATION,
    PROCESS_EFFECTIVE_BALANCE_UPDATES,
    PROCESS_PARTICIPATION_RECORD_UPDATES,
    PROCESS_SLASHINGS_RESET,
    PROCESS_ETH1_DATA_RESET,
    PROCESS_RANDAO_MIXES_RESET,
    PROCESS_HISTORICAL_ROOTS_UPDATE
  }

  void executeOperation(Operation operation, MutableBeaconState preState)
      throws EpochProcessingException;
}
