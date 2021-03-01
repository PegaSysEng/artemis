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

package tech.pegasys.teku.ssz.backing.collections.impl;

import java.util.function.Supplier;
import org.apache.tuweni.bytes.Bytes32;
import tech.pegasys.teku.ssz.backing.collections.SszBytes32Vector;
import tech.pegasys.teku.ssz.backing.collections.SszMutableBytes32Vector;
import tech.pegasys.teku.ssz.backing.schema.SszCompositeSchema;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.view.SszPrimitives.SszBytes32;

public class SszBytes32VectorImpl extends SszPrimitiveVectorImpl<Bytes32, SszBytes32>
    implements SszBytes32Vector {

  public SszBytes32VectorImpl(SszCompositeSchema<?> schema, Supplier<TreeNode> lazyBackingNode) {
    super(schema, lazyBackingNode);
  }

  public SszBytes32VectorImpl(SszCompositeSchema<?> schema, TreeNode backingNode) {
    super(schema, backingNode);
  }

  @Override
  public Bytes32 getBytes32(int index) {
    return getElement(index);
  }

  @Override
  public SszMutableBytes32Vector createWritableCopy() {
    return new SszMutableBytes32VectorImpl(this);
  }
}