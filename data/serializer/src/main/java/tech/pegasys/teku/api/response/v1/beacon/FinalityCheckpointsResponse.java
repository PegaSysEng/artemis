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

package tech.pegasys.teku.api.response.v1.beacon;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import tech.pegasys.teku.api.schema.Checkpoint;

public class FinalityCheckpointsResponse {
  @JsonProperty("previous_justified")
  public final Checkpoint previous_justified;

  @JsonProperty("current_justified")
  public final Checkpoint current_justified;

  @JsonProperty("finalized")
  public final Checkpoint finalized;

  @JsonCreator
  public FinalityCheckpointsResponse(
      @JsonProperty("previous_justified") Checkpoint previous_justified,
      @JsonProperty("current_justified") Checkpoint current_justified,
      @JsonProperty("finalized") Checkpoint finalized) {
    this.previous_justified = previous_justified;
    this.current_justified = current_justified;
    this.finalized = finalized;
  }
}
