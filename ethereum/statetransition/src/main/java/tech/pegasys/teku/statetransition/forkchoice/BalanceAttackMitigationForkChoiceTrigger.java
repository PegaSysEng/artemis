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

package tech.pegasys.teku.statetransition.forkchoice;

import java.util.concurrent.atomic.AtomicReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tech.pegasys.teku.infrastructure.async.SafeFuture;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

/**
 * A fork choice trigger that implements the LMD GHOST Balance Attack mitigation from HF1.
 *
 * @see <a href="https://hackmd.io/nyKbwIluQlWBH6sli4tJlA?view">Implementation plan</a>
 */
class BalanceAttackMitigationForkChoiceTrigger implements ForkChoiceTrigger {

  private static final Logger LOG = LogManager.getLogger();
  private final AtomicReference<ForkChoiceUpdate> latestCompletedForkChoice =
      new AtomicReference<>();
  private final ForkChoice forkChoice;

  BalanceAttackMitigationForkChoiceTrigger(final ForkChoice forkChoice) {
    this.forkChoice = forkChoice;
  }

  @Override
  public void onSlotStartedWhileSyncing(final UInt64 nodeSlot) {
    processHead(nodeSlot);
  }

  @Override
  public void onSlotStarted(final UInt64 nodeSlot) {
    processHead(nodeSlot);
  }

  @Override
  public void onAttestationsDueForSlot(final UInt64 nodeSlot) {}

  public SafeFuture<Void> prepareForBlockProduction(final UInt64 slot) {
    final ForkChoiceUpdate forkChoiceUpdate = processHead(slot);
    if (forkChoiceUpdate.nodeSlot.isGreaterThan(slot)) {
      return SafeFuture.COMPLETE;
    } else if (forkChoiceUpdate.nodeSlot.equals(slot)) {
      return forkChoiceUpdate.result;
    } else {
      // Only possible if processHead messed up somehow
      return SafeFuture.failedFuture(
          new IllegalStateException(
              "Requested fork choice be processed for slot "
                  + slot
                  + " but result indicates fork choice was only up to "
                  + forkChoiceUpdate.nodeSlot));
    }
  }

  protected ForkChoiceUpdate processHead(final UInt64 nodeSlot) {
    // Keep trying to get our slot processed until we or someone else gets it done
    while (true) {
      final ForkChoiceUpdate previousUpdate = latestCompletedForkChoice.get();
      if (previousUpdate != null && previousUpdate.nodeSlot.isGreaterThanOrEqualTo(nodeSlot)) {
        LOG.debug(
            "Skipping fork choice update for slot {} as high water mark is already {}",
            nodeSlot,
            previousUpdate.nodeSlot);
        return previousUpdate;
      }
      final ForkChoiceUpdate newUpdate = new ForkChoiceUpdate(nodeSlot);
      if (latestCompletedForkChoice.compareAndSet(previousUpdate, newUpdate)) {
        forkChoice.processHead(nodeSlot).propagateTo(newUpdate.result);
        return newUpdate;
      }
    }
  }

  private static class ForkChoiceUpdate {
    private final UInt64 nodeSlot;
    private final SafeFuture<Void> result = new SafeFuture<>();

    private ForkChoiceUpdate(final UInt64 nodeSlot) {
      this.nodeSlot = nodeSlot;
    }
  }
}
