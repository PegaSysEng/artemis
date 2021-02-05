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

import java.util.List;
import java.util.Map;
import tech.pegasys.teku.ssz.backing.ContainerViewRead;
import tech.pegasys.teku.ssz.backing.ContainerViewWriteRef;
import tech.pegasys.teku.ssz.backing.SszData;
import tech.pegasys.teku.ssz.backing.ViewWrite;
import tech.pegasys.teku.ssz.backing.cache.IntCache;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.tree.TreeUpdates;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;

public class ContainerViewWriteImpl extends AbstractCompositeViewWrite<SszData, ViewWrite>
    implements ContainerViewWriteRef {

  public ContainerViewWriteImpl(ContainerViewReadImpl backingImmutableView) {
    super(backingImmutableView);
  }

  @Override
  protected ContainerViewReadImpl createViewRead(
      TreeNode backingNode, IntCache<SszData> viewCache) {
    return new ContainerViewReadImpl(getType(), backingNode, viewCache);
  }

  @Override
  public ContainerViewType<?> getType() {
    return (ContainerViewType<?>) super.getType();
  }

  @Override
  public ContainerViewRead commitChanges() {
    return (ContainerViewRead) super.commitChanges();
  }

  @Override
  public ViewWrite createWritableCopy() {
    return super.createWritableCopy();
  }

  @Override
  protected void checkIndex(int index, boolean set) {
    if (index >= size()) {
      throw new IndexOutOfBoundsException(
          "Invalid index " + index + " for container with size " + size());
    }
  }

  @Override
  protected TreeUpdates packChanges(
      List<Map.Entry<Integer, SszData>> newChildValues, TreeNode original) {
    throw new UnsupportedOperationException("Packed values are not supported");
  }
}
