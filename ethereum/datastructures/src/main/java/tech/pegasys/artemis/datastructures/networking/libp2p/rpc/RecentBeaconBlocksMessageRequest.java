/*
 * Copyright 2019 ConsenSys AG.
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

package tech.pegasys.artemis.datastructures.networking.libp2p.rpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.artemis.util.SSZTypes.SSZContainer;
import tech.pegasys.artemis.util.sos.SimpleOffsetSerializable;

public final class RecentBeaconBlocksMessageRequest
    implements SimpleOffsetSerializable, SSZContainer {

  private final List<Bytes32> blockRoots;

  public RecentBeaconBlocksMessageRequest(List<Bytes32> blockRoots) {
    this.blockRoots = blockRoots;
  }

  @Override
  public int getSSZFieldCount() {
    return 1;
  }

  @Override
  public List<Bytes> get_fixed_parts() {
    List<Bytes> fixedPartsList =
        new ArrayList<>(List.of(SSZ.encode(writer -> writer.writeFixedBytesList(blockRoots))));
    return fixedPartsList;
  }

  @Override
  public int hashCode() {
    return Objects.hash(blockRoots);
  }

  @Override
  public boolean equals(Object obj) {
    if (Objects.isNull(obj)) {
      return false;
    }

    if (this == obj) {
      return true;
    }

    if (!(obj instanceof RecentBeaconBlocksMessageRequest)) {
      return false;
    }

    RecentBeaconBlocksMessageRequest other = (RecentBeaconBlocksMessageRequest) obj;
    return Objects.equals(this.blockRoots(), other.blockRoots());
  }

  public List<Bytes32> blockRoots() {
    return blockRoots;
  }
}
