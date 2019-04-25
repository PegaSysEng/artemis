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

package tech.pegasys.artemis.networking.p2p.hobbits;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@JsonDeserialize(using = BlockBodies.BlockBodiesDeserializer.class)
final class BlockBodies {

  static class BlockBodiesDeserializer extends StdDeserializer<BlockBodies> {

    protected BlockBodiesDeserializer() {
      super(BlockBodies.class);
    }

    @Override
    public BlockBodies deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
      JsonNode node = jp.getCodec().readTree(jp);
      Iterator<JsonNode> iterator = node.iterator();
      List<BlockBody> elts = new ArrayList<>();
      while (iterator.hasNext()) {
        JsonNode child = iterator.next();
        elts.add(new BlockBody());
      }

      return new BlockBodies(elts);
    }
  }

  static class BlockBody {

    /*
    'randao_reveal': 'bytes96',
    'eth1_data': Eth1Data,
    'proposer_slashings': [ProposerSlashing],
    'attester_slashings': [AttesterSlashing],
    'attestations': [Attestation],
    'deposits': [Deposit],
    'voluntary_exits': [VoluntaryExit],
    'transfers': [Transfer],
     */

    BlockBody() {
      // TODO fill body with the right info
    }
  }

  private final List<BlockBody> bodies;

  BlockBodies(List<BlockBody> rootsAndSlots) {
    this.bodies = rootsAndSlots;
  }

  @JsonValue
  public List<BlockBody> bodies() {
    return bodies;
  }
}
