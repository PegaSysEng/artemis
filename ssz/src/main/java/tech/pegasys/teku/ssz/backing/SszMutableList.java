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

package tech.pegasys.teku.ssz.backing;

import java.util.Collection;

/**
 * Mutable List view with immutable elements This type of list can be modified by setting or
 * appending immutable elements
 *
 * @param <ElementType> Type of list elements
 */
public interface SszMutableList<ElementType extends SszData>
    extends SszMutableComposite<ElementType>, SszList<ElementType> {

  @Override
  void set(int index, ElementType value);

  @Override
  SszList<ElementType> commitChanges();

  /**
   * Appends a new immutable value to the end of the list. Size is incremented
   *
   * @throws IndexOutOfBoundsException if size would exceed maxLength
   */
  default void append(ElementType value) {
    set(size(), value);
  }

  default void appendAll(Collection<ElementType> elements) {
    elements.forEach(this::append);
  }
}