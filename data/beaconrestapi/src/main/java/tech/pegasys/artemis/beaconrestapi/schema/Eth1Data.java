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

package tech.pegasys.artemis.beaconrestapi.schema;

import com.google.common.primitives.UnsignedLong;
import org.apache.tuweni.bytes.Bytes32;

public class Eth1Data {
  public Bytes32 deposit_root;
  public UnsignedLong deposit_count;
  public Bytes32 block_hash;

  public Eth1Data(tech.pegasys.artemis.datastructures.blocks.Eth1Data eth1Data) {
    this.deposit_root = eth1Data.getDeposit_root();
    this.deposit_count = eth1Data.getDeposit_count();
    this.block_hash = eth1Data.getBlock_hash();
  }
}
