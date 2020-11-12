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

package tech.pegasys.teku.api.response.v1;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

@JsonIgnoreProperties(ignoreUnknown = true)
public class HeadEvent {
  @JsonProperty(value = "slot", required = true)
  public final UInt64 slot;

  @JsonProperty("block")
  public final Bytes32 block;

  @JsonProperty("state")
  public final Bytes32 state;

  @JsonProperty("epoch_transition")
  public final boolean epochTransition;

  @JsonProperty("proposer_shuffling_pivot_root")
  public final Bytes32 proposerShufflingPivotRoot;

  @JsonProperty("attester_shuffling_pivot_root")
  public final Bytes32 attesterShufflingPivotRoot;

  @JsonCreator
  public HeadEvent(
      @JsonProperty(value = "slot", required = true) final UInt64 slot,
      @JsonProperty("block") final Bytes32 block,
      @JsonProperty("state") final Bytes32 state,
      @JsonProperty("epoch_transition") final boolean epochTransition,
      @JsonProperty("proposer_shuffling_pivot_root") final Bytes32 proposerShufflingPivotRoot,
      @JsonProperty("attester_shuffling_pivot_root") final Bytes32 attesterShufflingPivotRoot) {
    this.slot = slot;
    this.block = block;
    this.state = state;
    this.epochTransition = epochTransition;
    this.proposerShufflingPivotRoot = proposerShufflingPivotRoot;
    this.attesterShufflingPivotRoot = attesterShufflingPivotRoot;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final HeadEvent headEvent = (HeadEvent) o;
    return epochTransition == headEvent.epochTransition
        && Objects.equals(slot, headEvent.slot)
        && Objects.equals(block, headEvent.block)
        && Objects.equals(state, headEvent.state)
        && Objects.equals(proposerShufflingPivotRoot, headEvent.proposerShufflingPivotRoot)
        && Objects.equals(attesterShufflingPivotRoot, headEvent.attesterShufflingPivotRoot);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        slot,
        block,
        state,
        epochTransition,
        proposerShufflingPivotRoot,
        attesterShufflingPivotRoot);
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("slot", slot)
        .add("block", block)
        .add("state", state)
        .add("epochTransition", epochTransition)
        .add("proposerShufflingPivotRoot", proposerShufflingPivotRoot)
        .add("attesterShufflingPivotRoot", attesterShufflingPivotRoot)
        .toString();
  }
}
