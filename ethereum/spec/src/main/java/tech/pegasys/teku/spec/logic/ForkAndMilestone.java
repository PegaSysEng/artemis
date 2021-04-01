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

package tech.pegasys.teku.spec.logic;

import tech.pegasys.teku.spec.datastructures.state.Fork;

public class ForkAndMilestone {
  private final Fork fork;
  private final Milestone milestone;

  public ForkAndMilestone(final Fork fork, final Milestone milestone) {
    this.fork = fork;
    this.milestone = milestone;
  }

  public Fork getFork() {
    return fork;
  }

  public Milestone getMilestone() {
    return milestone;
  }
}
