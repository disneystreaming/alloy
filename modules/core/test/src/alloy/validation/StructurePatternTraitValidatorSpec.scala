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

import alloy.StructurePatternTrait
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.shapes.StringShape
import software.amazon.smithy.model.shapes.StructureShape
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.validation.ValidationEvent
import software.amazon.smithy.model.validation.Severity
import scala.jdk.CollectionConverters._
import software.amazon.smithy.model.shapes.MemberShape
import software.amazon.smithy.model.traits.RequiredTrait

final class StructurePatternTraitValidatorSpec extends munit.FunSuite {

  private val validator = new StructurePatternTraitValidator

  test("no error") {
    val targetId = ShapeId.fromParts("test", "MyStruct")
    val patternTrait = StructurePatternTrait
      .builder()
      .setPattern("{one}-{two}")
      .setTarget(targetId)
      .build()
    val stringShape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "MyString"))
      .addTrait(patternTrait)
      .build()
    val structShape = StructureShape
      .builder()
      .id(targetId)
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("one"))
          .target(ShapeId.fromParts("smithy.api", "String"))
          .addTrait(new RequiredTrait)
          .build()
      )
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("two"))
          .target(ShapeId.fromParts("smithy.api", "Integer"))
          .addTrait(new RequiredTrait)
          .build()
      )
      .build()

    val model =
      Model.assembler.disableValidation
        .addShapes(structShape, stringShape)
        .assemble()
        .unwrap()

    val result = validator.validate(model).asScala.toList

    assertEquals(result, List.empty)
  }

  test("optional structure member") {
    val targetId = ShapeId.fromParts("test", "MyStruct")
    val patternTrait = StructurePatternTrait
      .builder()
      .setPattern("{one}")
      .setTarget(targetId)
      .build()
    val stringShape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "MyString"))
      .addTrait(patternTrait)
      .build()
    val structShape = StructureShape
      .builder()
      .id(targetId)
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("one"))
          .target(ShapeId.fromParts("smithy.api", "String"))
          .build()
      )
      .build()

    val model =
      Model.assembler.disableValidation
        .addShapes(structShape, stringShape)
        .assemble()
        .unwrap()

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("StructurePatternTrait")
        .shape(stringShape)
        .severity(Severity.ERROR)
        .message(
          "Pattern params must not target optional structure members, but 'one' is optional"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test("structure has one more param than pattern") {
    val targetId = ShapeId.fromParts("test", "MyStruct")
    val patternTrait = StructurePatternTrait
      .builder()
      .setPattern("{one}-{two}")
      .setTarget(targetId)
      .build()
    val stringShape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "MyString"))
      .addTrait(patternTrait)
      .build()
    val structShape = StructureShape
      .builder()
      .id(targetId)
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("one"))
          .target(ShapeId.fromParts("smithy.api", "String"))
          .addTrait(new RequiredTrait)
          .build()
      )
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("two"))
          .target(ShapeId.fromParts("smithy.api", "Integer"))
          .addTrait(new RequiredTrait)
          .build()
      )
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("three"))
          .target(ShapeId.fromParts("smithy.api", "Long"))
          .addTrait(new RequiredTrait)
          .build()
      )
      .build()

    val model =
      Model.assembler.disableValidation
        .addShapes(structShape, stringShape)
        .assemble()
        .unwrap()

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("StructurePatternTrait")
        .shape(stringShape)
        .severity(Severity.ERROR)
        .message(
          "Did not find pattern params for the following members: three"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test("param non-simple shape") {
    val targetId = ShapeId.fromParts("test", "MyStruct")
    val patternTrait = StructurePatternTrait
      .builder()
      .setPattern("{one}")
      .setTarget(targetId)
      .build()
    val stringShape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "MyString"))
      .addTrait(patternTrait)
      .build()
    val otherStruct = StructureShape
      .builder()
      .id(ShapeId.fromParts("test", "OtherStruct"))
      .build()
    val structShape = StructureShape
      .builder()
      .id(targetId)
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("one"))
          .target(otherStruct.toShapeId)
          .addTrait(new RequiredTrait)
          .build()
      )
      .build()

    val model =
      Model.assembler.disableValidation
        .addShapes(otherStruct, structShape, stringShape)
        .assemble()
        .unwrap()

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("StructurePatternTrait")
        .shape(stringShape)
        .severity(Severity.ERROR)
        .message(
          "Pattern params must target simple shapes only, but 'one' targets 'test#OtherStruct'"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test("no separator between params") {
    val targetId = ShapeId.fromParts("test", "MyStruct")
    val patternTrait = StructurePatternTrait
      .builder()
      .setPattern("{one}{two}")
      .setTarget(targetId)
      .build()
    val stringShape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "MyString"))
      .addTrait(patternTrait)
      .build()
    val structShape = StructureShape
      .builder()
      .id(targetId)
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("one"))
          .target(ShapeId.fromParts("smithy.api", "String"))
          .addTrait(new RequiredTrait)
          .build()
      )
      .addMember(
        MemberShape
          .builder()
          .id(targetId.withMember("two"))
          .target(ShapeId.fromParts("smithy.api", "Integer"))
          .addTrait(new RequiredTrait)
          .build()
      )
      .build()

    val model =
      Model.assembler.disableValidation
        .addShapes(structShape, stringShape)
        .assemble()
        .unwrap()

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("StructurePatternTrait")
        .shape(stringShape)
        .severity(Severity.ERROR)
        .message(
          "Params must be separated by at least one character"
        )
        .build()
    )
    assertEquals(result, expected)
  }
}
