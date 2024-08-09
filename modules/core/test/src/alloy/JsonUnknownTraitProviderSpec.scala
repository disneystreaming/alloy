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
import software.amazon.smithy.model.shapes.DocumentShape
import software.amazon.smithy.model.shapes.MemberShape
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.shapes.StructureShape

import java.util.Optional

import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import software.amazon.smithy.model.validation.Severity

final class JsonUnknownTraitProviderSpec extends munit.FunSuite {

  test("has trait") {
    val documentShape = DocumentShape
      .builder()
      .id(ShapeId.fromParts("test", "MyDocument"))
      .build()
    val structId = ShapeId.fromParts("test", "MyStruct")
    val targetId = structId.withMember("myMap")
    val structShape = StructureShape
      .builder()
      .id(structId)
      .addMember(
        MemberShape
          .builder()
          .id(targetId)
          .target(documentShape.getId)
          .addTrait(new JsonUnknownTrait)
          .build()
      )
      .build()

    val model =
      Model.assembler
        .addShapes(structShape, documentShape)
        .assemble()
        .unwrap()

    val result = model
      .getShape(targetId)
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
         |structure MyStruct {
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
}
