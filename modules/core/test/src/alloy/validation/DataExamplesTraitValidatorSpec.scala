/* Copyright 2022 Disney Streaming
 *
 * Licensed under the Tomorrow Open Source Technology License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://disneystreaming.github.io/TOST-1.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package alloy.validation

import software.amazon.smithy.model.shapes.StringShape
import software.amazon.smithy.model.shapes.ShapeId
import alloy.DataExamplesTrait
import software.amazon.smithy.model.node.ObjectNode
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.validation.ValidationEvent
import scala.jdk.CollectionConverters._
import software.amazon.smithy.model.validation.Severity
import software.amazon.smithy.model.node.StringNode
import software.amazon.smithy.model.SourceLocation
import software.amazon.smithy.model.node.NumberNode
import software.amazon.smithy.model.shapes.IntegerShape

final class DataExamplesTraitValidatorSpec extends munit.FunSuite {

  private val validator = new DataExamplesTraitValidator

  test("find when node does not match shape") {
    val example = DataExamplesTrait
      .builder()
      .addExample(
        new DataExamplesTrait.DataExample(
          DataExamplesTrait.DataExampleType.SMITHY,
          ObjectNode.builder().withMember("something", false).build()
        )
      )
      .build()
    val shape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "TestString"))
      .addTrait(example)
      .build()
    val model = Model.builder().addShape(shape).build()
    val result = validator.validate(model).asScala.toList
    val expected = List(
      ValidationEvent
        .builder()
        .id("DataExamplesTrait")
        .shape(shape)
        .severity(Severity.ERROR)
        .message(
          "DataExample of `test#TestString`: Expected string value for string shape, `test#TestString`; found object value"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test("no errors when node matches shape") {
    val example = DataExamplesTrait
      .builder()
      .addExample(
        new DataExamplesTrait.DataExample(
          DataExamplesTrait.DataExampleType.SMITHY,
          new StringNode("something", SourceLocation.NONE)
        )
      )
      .build()
    val shape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "TestString"))
      .addTrait(example)
      .build()
    val model = Model.builder().addShape(shape).build()
    val result = validator.validate(model).asScala.toList
    val expected = List.empty
    assertEquals(result, expected)
  }

  test("no errors when JSON type") {
    val example = DataExamplesTrait
      .builder()
      .addExample(
        new DataExamplesTrait.DataExample(
          DataExamplesTrait.DataExampleType.JSON,
          new NumberNode(1, SourceLocation.NONE)
        )
      )
      .build()
    val shape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "TestString"))
      .addTrait(example)
      .build()
    val model = Model.builder().addShape(shape).build()
    val result = validator.validate(model).asScala.toList
    val expected = List.empty
    assertEquals(result, expected)
  }

  test("no errors when STRING type") {
    val example = DataExamplesTrait
      .builder()
      .addExample(
        new DataExamplesTrait.DataExample(
          DataExamplesTrait.DataExampleType.STRING,
          new StringNode("something", SourceLocation.NONE)
        )
      )
      .build()
    val shape = IntegerShape
      .builder()
      .id(ShapeId.fromParts("test", "TestString"))
      .addTrait(example)
      .build()
    val model = Model.builder().addShape(shape).build()
    val result = validator.validate(model).asScala.toList
    val expected = List.empty
    assertEquals(result, expected)
  }

}
