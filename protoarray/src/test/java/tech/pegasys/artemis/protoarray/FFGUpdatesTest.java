package tech.pegasys.artemis.protoarray;

import com.google.common.primitives.UnsignedLong;
import org.apache.tuweni.bytes.Bytes32;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.primitives.UnsignedLong.ONE;
import static com.google.common.primitives.UnsignedLong.ZERO;
import static com.google.common.primitives.UnsignedLong.valueOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static tech.pegasys.artemis.protoarray.HashUtil.getHash;

public class FFGUpdatesTest {

  @Test
  void case1() {

    ProtoArrayForkChoice forkChoice = ProtoArrayForkChoice.create(
            ZERO,
            Bytes32.ZERO,
            ONE,
            ONE,
            getHash(0)
    );

    List<UnsignedLong> balances = new ArrayList<>(List.of(valueOf(1), valueOf(1)));

    // Ensure that the head starts at the finalized block.
    assertThat(forkChoice.findHead(
            valueOf(0),
            getHash(0),
            valueOf(0),
            balances)).isEqualTo(getHash(0));

    // Build the following tree
    //
    //            0 <- just: 0, fin: 0
    //            |
    //            1 <- just: 0, fin: 0
    //            |
    //            2 <- just: 1, fin: 0
    //            |
    //            3 <- just: 2, fin: 1
    forkChoice.processBlock(
            ONE,
            getHash(1),
            getHash(0),
            Bytes32.ZERO,
            valueOf(0),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(2),
            getHash(2),
            getHash(1),
            Bytes32.ZERO,
            valueOf(1),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(3),
            getHash(3),
            getHash(2),
            Bytes32.ZERO,
            valueOf(2),
            valueOf(1));

    // Ensure that with justified epoch 0 we find 3
    //
    //            0 <- start
    //            |
    //            1
    //            |
    //            2
    //            |
    //            3 <- head
    assertThat(forkChoice.findHead(
            valueOf(0),
            getHash(0),
            valueOf(0),
            balances)).isEqualTo(getHash(3));

    // Ensure that with justified epoch 1 we find 2
    //
    //            0
    //            |
    //            1
    //            |
    //            2 <- start
    //            |
    //            3 <- head
    assertThat(forkChoice.findHead(
            valueOf(1),
            getHash(2),
            valueOf(0),
            balances)).isEqualTo(getHash(2));

    // Ensure that with justified epoch 2 we find 3
    //
    //            0
    //            |
    //            1
    //            |
    //            2
    //            |
    //            3 <- start + head
    assertThat(forkChoice.findHead(
            valueOf(2),
            getHash(3),
            valueOf(1),
            balances)).isEqualTo(getHash(3));
  }

  @Test
  void case2() {
    ProtoArrayForkChoice forkChoice = ProtoArrayForkChoice.create(
            ZERO,
            Bytes32.ZERO,
            ONE,
            ONE,
            getHash(0)
    );

    List<UnsignedLong> balances = new ArrayList<>(List.of(valueOf(1), valueOf(1)));

    // Ensure that the head starts at the finalized block.
    assertThat(forkChoice.findHead(
            valueOf(1),
            getHash(0),
            valueOf(1),
            balances)).isEqualTo(getHash(0));

    // Build the following tree.
    //
    //                       0
    //                      / \
    //  just: 0, fin: 0 -> 1   2 <- just: 0, fin: 0
    //                     |   |
    //  just: 1, fin: 0 -> 3   4 <- just: 0, fin: 0
    //                     |   |
    //  just: 1, fin: 0 -> 5   6 <- just: 0, fin: 0
    //                     |   |
    //  just: 1, fin: 0 -> 7   8 <- just: 1, fin: 0
    //                     |   |
    //  just: 2, fin: 0 -> 9  10 <- just: 2, fin: 0

    //  Left branch
    forkChoice.processBlock(
            valueOf(1),
            getHash(1),
            getHash(0),
            Bytes32.ZERO,
            valueOf(0),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(2),
            getHash(3),
            getHash(1),
            Bytes32.ZERO,
            valueOf(1),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(3),
            getHash(5),
            getHash(3),
            Bytes32.ZERO,
            valueOf(1),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(4),
            getHash(7),
            getHash(5),
            Bytes32.ZERO,
            valueOf(1),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(4),
            getHash(9),
            getHash(7),
            Bytes32.ZERO,
            valueOf(2),
            valueOf(0));


    //  Right branch
    forkChoice.processBlock(
            valueOf(1),
            getHash(2),
            getHash(0),
            Bytes32.ZERO,
            valueOf(0),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(2),
            getHash(4),
            getHash(2),
            Bytes32.ZERO,
            valueOf(0),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(3),
            getHash(6),
            getHash(4),
            Bytes32.ZERO,
            valueOf(0),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(4),
            getHash(8),
            getHash(6),
            Bytes32.ZERO,
            valueOf(1),
            valueOf(0));
    forkChoice.processBlock(
            valueOf(4),
            getHash(10),
            getHash(8),
            Bytes32.ZERO,
            valueOf(2),
            valueOf(0));

    // Ensure that if we start at 0 we find 10 (just: 0, fin: 0).
    //
    //           0  <-- start
    //          / \
    //         1   2
    //         |   |
    //         3   4
    //         |   |
    //         5   6
    //         |   |
    //         7   8
    //         |   |
    //         9  10 <-- head
    assertThat(forkChoice.findHead(
            valueOf(0),
            getHash(0),
            valueOf(0),
            balances)).isEqualTo(getHash(10));

    // Same as above, but with justified epoch 2.
    assertThat(forkChoice.findHead(
            valueOf(2),
            getHash(0),
            valueOf(0),
            balances)).isEqualTo(getHash(10));

    // Same as above, but with justified epoch 3 (should be invalid).
    assertThatThrownBy(() -> forkChoice.findHead(
            valueOf(3),
            getHash(0),
            valueOf(0),
            balances))
            .hasMessage("ProtoArray: Best node is not viable for head");

    // Add a vote to 1.
    //
    //                 0
    //                / \
    //    +1 vote -> 1   2
    //               |   |
    //               3   4
    //               |   |
    //               5   6
    //               |   |
    //               7   8
    //               |   |
    //               9  10
    forkChoice.processAttestation(
            0,
            getHash(1),
            valueOf(0)
    );

    // Ensure that if we start at 0 we find 9 (just: 0, fin: 0).
    //
    //           0  <-- start
    //          / \
    //         1   2
    //         |   |
    //         3   4
    //         |   |
    //         5   6
    //         |   |
    //         7   8
    //         |   |
    // head -> 9  10
    assertThat(forkChoice.findHead(
            valueOf(0),
            getHash(0),
            valueOf(0),
            balances)).isEqualTo(getHash(9));

    // Same as above but justified epoch 2.
    assertThat(forkChoice.findHead(
            valueOf(2),
            getHash(0),
            valueOf(0),
            balances)).isEqualTo(getHash(9));

    // Same as above but justified epoch 3 (should fail).
    assertThatThrownBy(() -> forkChoice.findHead(
            valueOf(3),
            getHash(0),
            valueOf(0),
            balances))
            .hasMessage("ProtoArray: Best node is not viable for head");

    // Add a vote to 2.
    //
    //                 0
    //                / \
    //               1   2 <- +1 vote
    //               |   |
    //               3   4
    //               |   |
    //               5   6
    //               |   |
    //               7   8
    //               |   |
    //               9  10
    forkChoice.processAttestation(
            1,
            getHash(2),
            valueOf(0)
    );

    // Ensure that if we start at 0 we find 10 (just: 0, fin: 0).
    //
    //           0  <-- start
    //          / \
    //         1   2
    //         |   |
    //         3   4
    //         |   |
    //         5   6
    //         |   |
    //         7   8
    //         |   |
    //         9  10 <-- head
    assertThat(forkChoice.findHead(
            valueOf(0),
            getHash(0),
            valueOf(0),
            balances)).isEqualTo(getHash(10));

    // Same as above but justified epoch 2.
    assertThat(forkChoice.findHead(
            valueOf(2),
            getHash(0),
            valueOf(0),
            balances)).isEqualTo(getHash(10));

    // Same as above but justified epoch 3 (should fail).
    assertThatThrownBy(() -> forkChoice.findHead(
            valueOf(3),
            getHash(0),
            valueOf(0),
            balances))
            .hasMessage("ProtoArray: Best node is not viable for head");

    // Ensure that if we start at 1 we find 9 (just: 0, fin: 0).
    //
    //            0
    //           / \
    //  start-> 1   2
    //          |   |
    //          3   4
    //          |   |
    //          5   6
    //          |   |
    //          7   8
    //          |   |
    //  head -> 9  10
    assertThat(forkChoice.findHead(
            valueOf(0),
            getHash(1),
            valueOf(0),
            balances)).isEqualTo(getHash(9));

    // Same as above but justified epoch 2.
    assertThat(forkChoice.findHead(
            valueOf(2),
            getHash(1),
            valueOf(0),
            balances)).isEqualTo(getHash(9));

    // Same as above but justified epoch 3 (should fail).
    assertThatThrownBy(() -> forkChoice.findHead(
            valueOf(3),
            getHash(1),
            valueOf(0),
            balances))
            .hasMessage("ProtoArray: Best node is not viable for head");

    // Ensure that if we start at 2 we find 10 (just: 0, fin: 0).
    //
    //            0
    //           / \
    //          1   2 <- start
    //          |   |
    //          3   4
    //          |   |
    //          5   6
    //          |   |
    //          7   8
    //          |   |
    //          9  10 <- head
    assertThat(forkChoice.findHead(
            valueOf(0),
            getHash(2),
            valueOf(0),
            balances)).isEqualTo(getHash(10));

    // Same as above but justified epoch 2.
    assertThat(forkChoice.findHead(
            valueOf(2),
            getHash(2),
            valueOf(0),
            balances)).isEqualTo(getHash(10));

    // Same as above but justified epoch 3 (should fail).
    assertThatThrownBy(() -> forkChoice.findHead(
            valueOf(3),
            getHash(2),
            valueOf(0),
            balances))
            .hasMessage("ProtoArray: Best node is not viable for head");
  }
}
