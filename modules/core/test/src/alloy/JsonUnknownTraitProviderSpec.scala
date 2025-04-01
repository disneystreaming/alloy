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

package alloy

import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.validation.Severity

import java.util.Optional
import scala.jdk.CollectionConverters.*
import scala.jdk.OptionConverters.*

final class JsonUnknownTraitProviderSpec extends munit.FunSuite {

  test("has trait") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy#jsonUnknown
         |
         |map UnknownProps {
         |  key: String
         |  value: Document
         |}
         |
         |structure MyStruct {
         |  @jsonUnknown
         |  myMap: UnknownProps
         |}
         |""".stripMargin

    val model =
      Model.assembler
        .discoverModels()
        .addUnparsedModel("/test.smithy", source)
        .assemble()
        .unwrap()

    val result = model
      .getShape(ShapeId.from("test#MyStruct$myMap"))
      .map(shape => shape.hasTrait(classOf[JsonUnknownTrait]))

    assertEquals(result, Optional.of(true))
  }

  test("trait can only be applied to a single member") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy#jsonUnknown
         |
         |map UnknownProps {
         |  key: String
         |  value: Document
         |}
         |
         |structure MyStruct {
         |  @jsonUnknown
         |  first: UnknownProps
         |  @jsonUnknown
         |  second: UnknownProps
         |}
         |""".stripMargin

    val result =
      Model.assembler
        .discoverModels()
        .addUnparsedModel("/test.smithy", source)
        .assemble()

    assert(result.isBroken())

    val errors = result
      .getValidationEvents()
      .asScala
      .filter(ev => ev.getSeverity() == Severity.ERROR)
      .toList

    assertEquals(errors.length, 1)

    val List(theError) = errors

    assertEquals(
      Some(ShapeId.from("test#MyStruct")),
      theError.getShapeId().toScala
    )
    assertEquals(theError.getId(), "ExclusiveStructureMemberTrait")

  }

  test("the trait can be applied to a union member document") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy#jsonUnknown
         |
         |union MyOpenUnion {
         |  first: String
         |  @jsonUnknown
         |  second: Document
         |}
         |""".stripMargin

    val result =
      Model.assembler
        .discoverModels()
        .addUnparsedModel("/test.smithy", source)
        .assemble()

    assert(!result.isBroken())
  }

  test("the trait cannot be applied to multiple union members") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy#jsonUnknown
         |
         |union MyOpenUnion {
         |  @jsonUnknown
         |  first: Document
         |  @jsonUnknown
         |  second: Document
         |}
         |""".stripMargin

    val result =
      Model.assembler
        .discoverModels()
        .addUnparsedModel("/test.smithy", source)
        .assemble()

    assert(result.isBroken())

    val eventIds = result
      .getValidationEvents()
      .asScala
      .map(_.getId())
      .toList

    assert(
      eventIds
        .contains("JsonUnknownTrait.ConflictingUnionMember"),
      s"validation events should contain one with ID: JsonUnknownTrait.ConflictingUnionMember. Found: $eventIds"
    )
  }

  test(
    "the trait cannot be applied to union members that don't target a document shape"
  ) {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy#jsonUnknown
         |
         |union MyOpenUnion {
         |  first: Document
         |  @jsonUnknown
         |  second: String
         |}
         |""".stripMargin

    val result =
      Model.assembler
        .discoverModels()
        .addUnparsedModel("/test.smithy", source)
        .assemble()

    assert(result.isBroken())

    val eventIds = result
      .getValidationEvents()
      .asScala
      .map(_.getId())
      .toList

    assert(
      eventIds
        .contains("TraitTarget"),
      s"validation events should contain one with ID: TraitTarget. Found: $eventIds"
    )
  }
}
