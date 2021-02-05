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
import tech.pegasys.teku.ssz.backing.SszContainer;
import tech.pegasys.teku.ssz.backing.SszData;
import tech.pegasys.teku.ssz.backing.tree.TreeNode;
import tech.pegasys.teku.ssz.backing.type.ContainerViewType;
import tech.pegasys.teku.ssz.backing.type.SszSchema;

/** Autogenerated by tech.pegasys.teku.ssz.backing.ContainersGenerator */
public abstract class ContainerType12<
        C extends SszContainer,
        V0 extends SszData,
        V1 extends SszData,
        V2 extends SszData,
        V3 extends SszData,
        V4 extends SszData,
        V5 extends SszData,
        V6 extends SszData,
        V7 extends SszData,
        V8 extends SszData,
        V9 extends SszData,
        V10 extends SszData,
        V11 extends SszData>
    extends ContainerViewType<C> {

  public static <
          C extends SszContainer,
          V0 extends SszData,
          V1 extends SszData,
          V2 extends SszData,
          V3 extends SszData,
          V4 extends SszData,
          V5 extends SszData,
          V6 extends SszData,
          V7 extends SszData,
          V8 extends SszData,
          V9 extends SszData,
          V10 extends SszData,
          V11 extends SszData>
      ContainerType12<C, V0, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11> create(
          SszSchema<V0> fieldType0,
          SszSchema<V1> fieldType1,
          SszSchema<V2> fieldType2,
          SszSchema<V3> fieldType3,
          SszSchema<V4> fieldType4,
          SszSchema<V5> fieldType5,
          SszSchema<V6> fieldType6,
          SszSchema<V7> fieldType7,
          SszSchema<V8> fieldType8,
          SszSchema<V9> fieldType9,
          SszSchema<V10> fieldType10,
          SszSchema<V11> fieldType11,
          BiFunction<
                  ContainerType12<C, V0, V1, V2, V3, V4, V5, V6, V7, V8, V9, V10, V11>, TreeNode, C>
              instanceCtor) {
    return new ContainerType12<>(
        fieldType0,
        fieldType1,
        fieldType2,
        fieldType3,
        fieldType4,
        fieldType5,
        fieldType6,
        fieldType7,
        fieldType8,
        fieldType9,
        fieldType10,
        fieldType11) {
      @Override
      public C createFromBackingNode(TreeNode node) {
        return instanceCtor.apply(this, node);
      }
    };
  }

  protected ContainerType12(
      SszSchema<V0> fieldType0,
      SszSchema<V1> fieldType1,
      SszSchema<V2> fieldType2,
      SszSchema<V3> fieldType3,
      SszSchema<V4> fieldType4,
      SszSchema<V5> fieldType5,
      SszSchema<V6> fieldType6,
      SszSchema<V7> fieldType7,
      SszSchema<V8> fieldType8,
      SszSchema<V9> fieldType9,
      SszSchema<V10> fieldType10,
      SszSchema<V11> fieldType11) {

    super(
        List.of(
            fieldType0,
            fieldType1,
            fieldType2,
            fieldType3,
            fieldType4,
            fieldType5,
            fieldType6,
            fieldType7,
            fieldType8,
            fieldType9,
            fieldType10,
            fieldType11));
  }

  protected ContainerType12(
      String containerName,
      NamedType<V0> fieldNamedType0,
      NamedType<V1> fieldNamedType1,
      NamedType<V2> fieldNamedType2,
      NamedType<V3> fieldNamedType3,
      NamedType<V4> fieldNamedType4,
      NamedType<V5> fieldNamedType5,
      NamedType<V6> fieldNamedType6,
      NamedType<V7> fieldNamedType7,
      NamedType<V8> fieldNamedType8,
      NamedType<V9> fieldNamedType9,
      NamedType<V10> fieldNamedType10,
      NamedType<V11> fieldNamedType11) {

    super(
        containerName,
        List.of(
            fieldNamedType0,
            fieldNamedType1,
            fieldNamedType2,
            fieldNamedType3,
            fieldNamedType4,
            fieldNamedType5,
            fieldNamedType6,
            fieldNamedType7,
            fieldNamedType8,
            fieldNamedType9,
            fieldNamedType10,
            fieldNamedType11));
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V0> getFieldType0() {
    return (SszSchema<V0>) getChildType(0);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V1> getFieldType1() {
    return (SszSchema<V1>) getChildType(1);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V2> getFieldType2() {
    return (SszSchema<V2>) getChildType(2);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V3> getFieldType3() {
    return (SszSchema<V3>) getChildType(3);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V4> getFieldType4() {
    return (SszSchema<V4>) getChildType(4);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V5> getFieldType5() {
    return (SszSchema<V5>) getChildType(5);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V6> getFieldType6() {
    return (SszSchema<V6>) getChildType(6);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V7> getFieldType7() {
    return (SszSchema<V7>) getChildType(7);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V8> getFieldType8() {
    return (SszSchema<V8>) getChildType(8);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V9> getFieldType9() {
    return (SszSchema<V9>) getChildType(9);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V10> getFieldType10() {
    return (SszSchema<V10>) getChildType(10);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V11> getFieldType11() {
    return (SszSchema<V11>) getChildType(11);
  }
}
