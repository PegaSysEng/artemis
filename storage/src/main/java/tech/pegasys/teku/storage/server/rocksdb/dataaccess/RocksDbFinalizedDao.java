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

package tech.pegasys.teku.storage.server.rocksdb.dataaccess;

import com.google.common.primitives.UnsignedLong;
import com.google.errorprone.annotations.MustBeClosed;
import java.util.Optional;
import java.util.stream.Stream;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.datastructures.blocks.SignedBeaconBlock;
import tech.pegasys.teku.datastructures.blocks.SlotAndBlockRoot;
import tech.pegasys.teku.datastructures.state.BeaconState;

/**
 * Provides an abstract "data access object" interface for working with finalized data from the
 * underlying database.
 */
public interface RocksDbFinalizedDao extends AutoCloseable {

  Optional<SignedBeaconBlock> getFinalizedBlock(final Bytes32 root);

  FinalizedUpdater finalizedUpdater();

  Optional<SignedBeaconBlock> getFinalizedBlockAtSlot(UnsignedLong slot);

  Optional<SignedBeaconBlock> getLatestFinalizedBlockAtSlot(UnsignedLong slot);

  Optional<BeaconState> getLatestAvailableFinalizedState(UnsignedLong maxSlot);

  @MustBeClosed
  Stream<SignedBeaconBlock> streamFinalizedBlocks(UnsignedLong startSlot, UnsignedLong endSlot);

  Optional<UnsignedLong> getSlotForFinalizedBlockRoot(Bytes32 blockRoot);

  Optional<UnsignedLong> getSlotForFinalizedStateRoot(Bytes32 stateRoot);

  Optional<SlotAndBlockRoot> getSlotAndBlockRootForFinalizedStateRoot(Bytes32 stateRoot);

  interface FinalizedUpdater extends AutoCloseable {

    void addFinalizedBlock(final SignedBeaconBlock block);

    void addFinalizedState(final Bytes32 blockRoot, final BeaconState state);

    void addFinalizedStateRoot(final Bytes32 stateRoot, final UnsignedLong slot);

    void commit();

    void cancel();

    @Override
    void close();
  }
}
