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

package tech.pegasys.teku.api.schema;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tech.pegasys.teku.spec.util.DataStructureUtil;

public class ValidatorTest {
  private final DataStructureUtil dataStructureUtil = new DataStructureUtil();
  private final tech.pegasys.teku.spec.datastructures.state.Validator validatorInternal =
      dataStructureUtil.randomValidator();

  @Test
  public void shouldConvertToInternalObject() {
    final Validator validator = new Validator(validatorInternal);
    assertThat(validator.asInternalValidator()).isEqualTo(validatorInternal);
  }
}
