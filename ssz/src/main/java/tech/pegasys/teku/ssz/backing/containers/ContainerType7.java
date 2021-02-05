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

package tech.pegasys.teku.ssz.backing.containers;

import java.util.List;
import java.util.function.BiFunction;
import tech.pegasys.teku.ssz.backing.ContainerViewRead;
import tech.pegasys.teku.ssz.backing.SszData;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;
import tech.pegasys.teku.ssz.backing.type.ViewType;

/** Autogenerated by tech.pegasys.teku.ssz.backing.ContainersGenerator */
public abstract class ContainerType7<
        C extends ContainerViewRead,
        V0 extends SszData,
        V1 extends SszData,
        V2 extends SszData,
        V3 extends SszData,
        V4 extends SszData,
        V5 extends SszData,
        V6 extends SszData>
    extends ContainerViewType<C> {

  public static <
          C extends ContainerViewRead,
          V0 extends SszData,
          V1 extends SszData,
          V2 extends SszData,
          V3 extends SszData,
          V4 extends SszData,
          V5 extends SszData,
          V6 extends SszData>
      ContainerType7<C, V0, V1, V2, V3, V4, V5, V6> create(
          ViewType<V0> fieldType0,
          ViewType<V1> fieldType1,
          ViewType<V2> fieldType2,
          ViewType<V3> fieldType3,
          ViewType<V4> fieldType4,
          ViewType<V5> fieldType5,
          ViewType<V6> fieldType6,
          BiFunction<ContainerType7<C, V0, V1, V2, V3, V4, V5, V6>, TreeNode, C> instanceCtor) {
    return new ContainerType7<>(
        fieldType0, fieldType1, fieldType2, fieldType3, fieldType4, fieldType5, fieldType6) {
      @Override
      public C createFromBackingNode(TreeNode node) {
        return instanceCtor.apply(this, node);
      }
    };
  }

  protected ContainerType7(
      ViewType<V0> fieldType0,
      ViewType<V1> fieldType1,
      ViewType<V2> fieldType2,
      ViewType<V3> fieldType3,
      ViewType<V4> fieldType4,
      ViewType<V5> fieldType5,
      ViewType<V6> fieldType6) {

    super(
        List.of(
            fieldType0, fieldType1, fieldType2, fieldType3, fieldType4, fieldType5, fieldType6));
  }

  protected ContainerType7(
      String containerName,
      NamedType<V0> fieldNamedType0,
      NamedType<V1> fieldNamedType1,
      NamedType<V2> fieldNamedType2,
      NamedType<V3> fieldNamedType3,
      NamedType<V4> fieldNamedType4,
      NamedType<V5> fieldNamedType5,
      NamedType<V6> fieldNamedType6) {

    super(
        containerName,
        List.of(
            fieldNamedType0,
            fieldNamedType1,
            fieldNamedType2,
            fieldNamedType3,
            fieldNamedType4,
            fieldNamedType5,
            fieldNamedType6));
  }

  @SuppressWarnings("unchecked")
  public ViewType<V0> getFieldType0() {
    return (ViewType<V0>) getChildType(0);
  }

  @SuppressWarnings("unchecked")
  public ViewType<V1> getFieldType1() {
    return (ViewType<V1>) getChildType(1);
  }

  @SuppressWarnings("unchecked")
  public ViewType<V2> getFieldType2() {
    return (ViewType<V2>) getChildType(2);
  }

  @SuppressWarnings("unchecked")
  public ViewType<V3> getFieldType3() {
    return (ViewType<V3>) getChildType(3);
  }

  @SuppressWarnings("unchecked")
  public ViewType<V4> getFieldType4() {
    return (ViewType<V4>) getChildType(4);
  }

  @SuppressWarnings("unchecked")
  public ViewType<V5> getFieldType5() {
    return (ViewType<V5>) getChildType(5);
  }

  @SuppressWarnings("unchecked")
  public ViewType<V6> getFieldType6() {
    return (ViewType<V6>) getChildType(6);
  }
}
