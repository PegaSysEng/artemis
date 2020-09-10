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

package tech.pegasys.teku.sync.multipeer.batches;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.infrastructure.async.eventthread.EventThread;
import tech.pegasys.teku.infrastructure.unsigned.UInt64;

public class EventThreadOnlyBatch implements Batch {
  private final EventThread eventThread;
  private final StubBatch delegate;

  public EventThreadOnlyBatch(final EventThread eventThread, final StubBatch delegate) {
    this.eventThread = eventThread;
    this.delegate = delegate;
  }

  @Override
  public UInt64 getFirstSlot() {
    eventThread.checkOnEventThread();
    return delegate.getFirstSlot();
  }

  @Override
  public UInt64 getLastSlot() {
    eventThread.checkOnEventThread();
    return delegate.getLastSlot();
  }

  @Override
  public Optional<SignedBeaconBlock> getFirstBlock() {
    eventThread.checkOnEventThread();
    return delegate.getFirstBlock();
  }

  @Override
  public Optional<SignedBeaconBlock> getLastBlock() {
    eventThread.checkOnEventThread();
    return delegate.getLastBlock();
  }

  @Override
  public void markComplete() {
    eventThread.checkOnEventThread();
    delegate.markComplete();
  }

  @Override
  public boolean isComplete() {
    eventThread.checkOnEventThread();
    return delegate.isComplete();
  }

  @Override
  public boolean isConfirmed() {
    eventThread.checkOnEventThread();
    return delegate.isConfirmed();
  }

  @Override
  public boolean isFirstBlockConfirmed() {
    eventThread.checkOnEventThread();
    return delegate.isFirstBlockConfirmed();
  }

  @Override
  public boolean isImporting() {
    eventThread.checkOnEventThread();
    return delegate.isImporting();
  }

  @Override
  public boolean isContested() {
    eventThread.checkOnEventThread();
    return delegate.isContested();
  }

  @Override
  public void markFirstBlockConfirmed() {
    eventThread.checkOnEventThread();
    delegate.markFirstBlockConfirmed();
  }

  @Override
  public void markLastBlockConfirmed() {
    eventThread.checkOnEventThread();
    delegate.markLastBlockConfirmed();
  }

  @Override
  public void markAsContested() {
    eventThread.checkOnEventThread();
    delegate.markAsContested();
  }

  @Override
  public boolean isEmpty() {
    eventThread.checkOnEventThread();
    return delegate.isEmpty();
  }

  @Override
  public boolean isAwaitingBlocks() {
    eventThread.checkOnEventThread();
    return delegate.isAwaitingBlocks();
  }

  @Override
  public void requestMoreBlocks(final Runnable callback) {
    eventThread.checkOnEventThread();
    delegate.requestMoreBlocks(callback);
  }

  @Override
  public void markAsInvalid() {
    eventThread.checkOnEventThread();
    delegate.markAsInvalid();
  }

  public StubBatch getDelegate() {
    return delegate;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this).add("delegate", delegate).toString();
  }
}
