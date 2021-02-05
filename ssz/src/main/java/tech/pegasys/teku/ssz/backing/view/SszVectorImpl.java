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

package tech.pegasys.teku.ssz.backing.view;

import java.util.Objects;
import java.util.stream.Collectors;
import tech.pegasys.teku.ssz.backing.SszData;
import tech.pegasys.teku.ssz.backing.SszVector;
import tech.pegasys.teku.ssz.backing.cache.ArrayIntCache;
import tech.pegasys.teku.ssz.backing.cache.IntCache;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.type.CompositeViewType;
import tech.pegasys.teku.ssz.backing.type.VectorViewType;
import tech.pegasys.teku.ssz.backing.type.SszSchema;

public class SszVectorImpl<ElementReadType extends SszData>
    extends AbstractSszComposite<ElementReadType> implements SszVector<ElementReadType> {

  public SszVectorImpl(CompositeViewType<?> type, TreeNode backingNode) {
    super(type, backingNode);
  }

  public SszVectorImpl(
      CompositeViewType<?> type, TreeNode backingNode, IntCache<ElementReadType> cache) {
    super(type, backingNode, cache);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected ElementReadType getImpl(int index) {
    VectorViewType<ElementReadType> type = getType();
    SszSchema<?> elementType = type.getElementType();
    TreeNode node =
        getBackingNode().get(type.getGeneralizedIndex(index / type.getElementsPerChunk()));
    return (ElementReadType)
        elementType.createFromBackingNode(node, index % type.getElementsPerChunk());
  }

  @Override
  protected int sizeImpl() {
    return (int) Long.min(Integer.MAX_VALUE, getType().getMaxLength());
  }

  @Override
  public SszMutableVectorImpl<ElementReadType, ?> createWritableCopy() {
    return new SszMutableVectorImpl<>(this);
  }

  @SuppressWarnings("unchecked")
  @Override
  public VectorViewType<ElementReadType> getType() {
    return (VectorViewType<ElementReadType>) super.getType();
  }

  @Override
  protected IntCache<ElementReadType> createCache() {
    return size() > 16384 ? new ArrayIntCache<>() : new ArrayIntCache<>(size());
  }

  @Override
  protected void checkIndex(int index) {
    if (index >= size()) {
      throw new IndexOutOfBoundsException(
          "Invalid index " + index + " for vector with size " + size());
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof SszVector)) {
      return false;
    }
    return hashTreeRoot().equals(((SszData) o).hashTreeRoot());
  }

  @Override
  public int hashCode() {
    return Objects.hash(hashTreeRoot());
  }

  @Override
  public String toString() {
    return "SszVector{" + stream().map(Object::toString).collect(Collectors.joining()) + "}";
  }
}
