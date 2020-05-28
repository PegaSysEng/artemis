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

package tech.pegasys.teku.storage.server.rocksdb.serialization;

import com.google.common.primitives.UnsignedLong;
import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.bytes.Bytes32;
import org.apache.tuweni.ssz.SSZ;
import tech.pegasys.teku.pow.event.MinGenesisTimeBlockEvent;

public class MinGenesisTimeBlockEventSerializer
    implements RocksDbSerializer<MinGenesisTimeBlockEvent> {
  @Override
  public MinGenesisTimeBlockEvent deserialize(final byte[] data) {
    return SSZ.decode(
        Bytes.of(data),
        reader -> {
          final UnsignedLong timestamp = UnsignedLong.fromLongBits(reader.readUInt64());
          final UnsignedLong blockNumber = UnsignedLong.fromLongBits(reader.readUInt64());
          final Bytes32 blockHash = Bytes32.wrap(reader.readFixedBytes(Bytes32.SIZE));
          return new MinGenesisTimeBlockEvent(timestamp, blockNumber, blockHash);
        });
  }

  @Override
  public byte[] serialize(final MinGenesisTimeBlockEvent value) {
    Bytes bytes =
        SSZ.encode(
            writer -> {
              writer.writeUInt64(value.getTimestamp().longValue());
              writer.writeUInt64(value.getBlockNumber().longValue());
              writer.writeFixedBytes(value.getBlockHash());
            });
    return bytes.toArrayUnsafe();
  }
}
