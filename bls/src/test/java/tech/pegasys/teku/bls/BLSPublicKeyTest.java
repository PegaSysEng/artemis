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

package tech.pegasys.teku.bls;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.tuweni.bytes.Bytes;
import org.apache.tuweni.ssz.SSZ;
import org.junit.jupiter.api.Test;
import tech.pegasys.teku.bls.mikuli.G1Point;
import tech.pegasys.teku.bls.mikuli.PublicKey;

class BLSPublicKeyTest {

  @Test
  void isValidReturnsTrueForValidKey() {
    BLSPublicKey publicKey = BLSPublicKey.random(1);
    assertTrue(publicKey.isValid());
  }

  @Test
  void isValidReturnsFalseForInvalidKey() {
    BLSPublicKey publicKey = BLSPublicKey.random(1);
    BLSPublicKey invalidPublicKey = BLSPublicKey.fromBytes(publicKey.toBytes().shiftLeft(1));
    assertFalse(invalidPublicKey.isValid());
  }

  @Test
  void succeedsWhenEqualsReturnsTrueForTheSameEmptyPublicKey() {
    BLSPublicKey publicKey = BLSPublicKey.empty();
    assertEquals(publicKey, publicKey);
  }

  @Test
  void succeedsWhenTwoInfinityPublicKeysAreEqual() {
    // Infinity keys are valid G1 points, so pass the equality test
    BLSPublicKey publicKey1 = new BLSPublicKey(new PublicKey(new G1Point()));
    BLSPublicKey publicKey2 = new BLSPublicKey(new PublicKey(new G1Point()));
    assertEquals(publicKey1, publicKey2);
  }

  @Test
  void succeedsWhenInvalidPublicKeyIsInvalid() {
    BLSPublicKey invalidPublicKey =
        BLSPublicKey.fromBytesCompressed(
            Bytes.fromHexString(
                "0x9378a6e3984e96d2cd50450c76ca14732f1300efa04aecdb805b22e6d6926a85ef409e8f3acf494a1481090bf32ce3bd"));
    assertFalse(invalidPublicKey.isValid());
  }

  @Test
  void succeedsWhenComparingInvalidAndValidPublicKeyFails() {
    BLSPublicKey invalidPublicKey =
        BLSPublicKey.fromBytesCompressed(
            Bytes.fromHexString(
                "0x9378a6e3984e96d2cd50450c76ca14732f1300efa04aecdb805b22e6d6926a85ef409e8f3acf494a1481090bf32ce3bd"));
    BLSPublicKey validPublicKey =
        BLSPublicKey.fromBytesCompressed(
            Bytes.fromHexString(
                "0xb51aa9cdb40ed3e7e5a9b3323550fe323ecd5c7f5cb3d8b47af55a061811bc7da0397986cad0d565c0bdbbe99af24355"));
    assertFalse(invalidPublicKey.isValid());
    assertTrue(validPublicKey.isValid());
    assertNotEquals(validPublicKey, invalidPublicKey);
  }

  @Test
  void succeedsWhenInvalidPublicReturnsHashCode() {
    BLSPublicKey invalidPublicKey =
        BLSPublicKey.fromBytesCompressed(
            Bytes.fromHexString(
                "0x9378a6e3984e96d2cd50450c76ca14732f1300efa04aecdb805b22e6d6926a85ef409e8f3acf494a1481090bf32ce3bd"));
    BLSPublicKey validPublicKey =
        BLSPublicKey.fromBytesCompressed(
            Bytes.fromHexString(
                "0xb51aa9cdb40ed3e7e5a9b3323550fe323ecd5c7f5cb3d8b47af55a061811bc7da0397986cad0d565c0bdbbe99af24355"));
    assertNotEquals(invalidPublicKey.hashCode(), validPublicKey.hashCode());
  }

  @Test
  void succeedsIfSerializationOfEmptyPublicKeyIsCorrect() {
    BLSPublicKey emptyPublicKey = BLSPublicKey.empty();
    assertEquals(
        "0x000000000000000000000000000000000000000000000000"
            + "000000000000000000000000000000000000000000000000",
        emptyPublicKey.toBytes().toHexString());
  }

  @Test
  void succeedsIfDeserializationOfInfinityPublicKeyIsCorrect() {
    BLSPublicKey infinityPublicKey = new BLSPublicKey(new PublicKey(new G1Point()));
    byte[] pointBytes = new byte[48];
    pointBytes[0] = (byte) 0xc0;
    Bytes infinityBytesSsz =
        SSZ.encode(
            writer -> {
              writer.writeFixedBytes(Bytes.wrap(pointBytes));
            });
    BLSPublicKey deserializedPublicKey = BLSPublicKey.fromBytes(infinityBytesSsz);
    assertEquals(infinityPublicKey, deserializedPublicKey);
  }

  @Test
  void succeedsIfDeserializationThrowsWithTooFewBytes() {
    Bytes tooFewBytes = Bytes.wrap(new byte[51]);
    assertThrows(IllegalArgumentException.class, () -> BLSPublicKey.fromBytes(tooFewBytes));
  }

  @Test
  void succeedsWhenEqualsReturnsTrueForTheSamePublicKey() {
    BLSPublicKey publicKey = BLSPublicKey.random(42);
    assertEquals(publicKey, publicKey);
  }

  @Test
  void succeedsWhenEqualsReturnsTrueForIdenticalPublicKeys() {
    BLSPublicKey publicKey = BLSPublicKey.random(42);
    BLSPublicKey copyOfPublicKey = new BLSPublicKey(publicKey);
    assertEquals(publicKey, copyOfPublicKey);
  }

  @Test
  void succeedsWhenEqualsReturnsFalseForDifferentPublicKeys() {
    BLSPublicKey publicKey1 = BLSPublicKey.random(1);
    BLSPublicKey publicKey2 = BLSPublicKey.random(2);
    assertNotEquals(publicKey1, publicKey2);
  }

  @Test
  public void succeedsWhenEqualsReturnsTrueForEquivalentPublicKeysCreatedFromDifferentRawBytes() {
    BLSPublicKey publicKey1 = BLSPublicKey.random(1);
    Bytes expandedBytes = publicKey1.getPublicKey().g1Point().toBytes();
    Bytes compressedBytes = publicKey1.toBytesCompressed();
    assertNotEquals(expandedBytes, compressedBytes);

    BLSPublicKey publicKey2 = new BLSPublicKey(PublicKey.fromBytesCompressed(expandedBytes));
    BLSPublicKey publicKey3 = BLSPublicKey.fromBytes(compressedBytes);
    assertEquals(publicKey2, publicKey3);
  }

  @Test
  void succeedsWhenRoundtripSSZReturnsTheSamePublicKey() {
    BLSPublicKey publicKey1 = BLSPublicKey.random(42);
    BLSPublicKey publicKey2 = BLSPublicKey.fromBytes(publicKey1.toBytes());
    assertEquals(publicKey1, publicKey2);
  }

  @Test
  void succeedsWhenRoundtripSSZReturnsTheInfinityPublicKey() {
    BLSPublicKey publicKey1 = new BLSPublicKey(new PublicKey(new G1Point()));
    BLSPublicKey publicKey2 = BLSPublicKey.fromBytes(publicKey1.toBytes());
    assertEquals(publicKey1, publicKey2);
  }
}
