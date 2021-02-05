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
import tech.pegasys.teku.ssz.backing.schema.SszContainerSchema;
import tech.pegasys.teku.ssz.backing.schema.SszSchema;

/** Autogenerated by tech.pegasys.teku.ssz.backing.ContainersGenerator */
public abstract class ContainerType9<
        C extends SszContainer,
        V0 extends SszData,
        V1 extends SszData,
        V2 extends SszData,
        V3 extends SszData,
        V4 extends SszData,
        V5 extends SszData,
        V6 extends SszData,
        V7 extends SszData,
        V8 extends SszData>
    extends SszContainerSchema<C> {

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
          V8 extends SszData>
      ContainerType9<C, V0, V1, V2, V3, V4, V5, V6, V7, V8> create(
          SszSchema<V0> fieldType0,
          SszSchema<V1> fieldType1,
          SszSchema<V2> fieldType2,
          SszSchema<V3> fieldType3,
          SszSchema<V4> fieldType4,
          SszSchema<V5> fieldType5,
          SszSchema<V6> fieldType6,
          SszSchema<V7> fieldType7,
          SszSchema<V8> fieldType8,
          BiFunction<ContainerType9<C, V0, V1, V2, V3, V4, V5, V6, V7, V8>, TreeNode, C>
              instanceCtor) {
    return new ContainerType9<>(
        fieldType0,
        fieldType1,
        fieldType2,
        fieldType3,
        fieldType4,
        fieldType5,
        fieldType6,
        fieldType7,
        fieldType8) {
      @Override
      public C createFromBackingNode(TreeNode node) {
        return instanceCtor.apply(this, node);
      }
    };
  }

  protected ContainerType9(
      SszSchema<V0> fieldType0,
      SszSchema<V1> fieldType1,
      SszSchema<V2> fieldType2,
      SszSchema<V3> fieldType3,
      SszSchema<V4> fieldType4,
      SszSchema<V5> fieldType5,
      SszSchema<V6> fieldType6,
      SszSchema<V7> fieldType7,
      SszSchema<V8> fieldType8) {

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
            fieldType8));
  }

  protected ContainerType9(
      String containerName,
      NamedSchema<V0> fieldNamedSchema0,
      NamedSchema<V1> fieldNamedSchema1,
      NamedSchema<V2> fieldNamedSchema2,
      NamedSchema<V3> fieldNamedSchema3,
      NamedSchema<V4> fieldNamedSchema4,
      NamedSchema<V5> fieldNamedSchema5,
      NamedSchema<V6> fieldNamedSchema6,
      NamedSchema<V7> fieldNamedSchema7,
      NamedSchema<V8> fieldNamedSchema8) {

    super(
        containerName,
        List.of(
            fieldNamedSchema0,
            fieldNamedSchema1,
            fieldNamedSchema2,
            fieldNamedSchema3,
            fieldNamedSchema4,
            fieldNamedSchema5,
            fieldNamedSchema6,
            fieldNamedSchema7,
            fieldNamedSchema8));
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V0> getFieldType0() {
    return (SszSchema<V0>) getChildSchema(0);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V1> getFieldType1() {
    return (SszSchema<V1>) getChildSchema(1);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V2> getFieldType2() {
    return (SszSchema<V2>) getChildSchema(2);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V3> getFieldType3() {
    return (SszSchema<V3>) getChildSchema(3);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V4> getFieldType4() {
    return (SszSchema<V4>) getChildSchema(4);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V5> getFieldType5() {
    return (SszSchema<V5>) getChildSchema(5);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V6> getFieldType6() {
    return (SszSchema<V6>) getChildSchema(6);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V7> getFieldType7() {
    return (SszSchema<V7>) getChildSchema(7);
  }

  @SuppressWarnings("unchecked")
  public SszSchema<V8> getFieldType8() {
    return (SszSchema<V8>) getChildSchema(8);
  }
}
