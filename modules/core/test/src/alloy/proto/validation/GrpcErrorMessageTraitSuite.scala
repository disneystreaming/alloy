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

package alloy.proto.validation

import munit.FunSuite
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.SourceLocation
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.validation.Severity
import software.amazon.smithy.model.validation.ValidationEvent

import scala.jdk.CollectionConverters._

class GrpcErrorMessageTraitSuite extends FunSuite {

  test("single @grpcErrorMessage member is valid") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy.proto#grpcError
         |use alloy.proto#grpcErrorMessage
         |
         |@error("client")
         |@grpcError(code: 3)
         |structure MyError {
         |    @grpcErrorMessage
         |    message: String
         |    code: Integer
         |}
         |""".stripMargin

    val result = Model.assembler
      .discoverModels()
      .addUnparsedModel("/test.smithy", source)
      .assemble()

    val errorEvents = result.getValidationEvents.asScala
      .filter(_.getSeverity == Severity.ERROR)
      .toList

    assertEquals(errorEvents, Nil)
  }

  test("multiple @grpcErrorMessage members on same structure is invalid") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy.proto#grpcError
         |use alloy.proto#grpcErrorMessage
         |
         |@error("client")
         |@grpcError(code: 3)
         |structure MultipleError {
         |    @grpcErrorMessage
         |    message: String
         |    @grpcErrorMessage
         |    detail: String
         |}
         |""".stripMargin

    def normalize(e: ValidationEvent): ValidationEvent =
      e.toBuilder.sourceLocation(SourceLocation.NONE).build()

    val result = Model.assembler
      .discoverModels()
      .addUnparsedModel("/test.smithy", source)
      .assemble()

    val errorEvents = result.getValidationEvents.asScala
      .filter(_.getSeverity == Severity.ERROR)
      .toList

    val expectedError =
      ValidationEvent
        .builder()
        .id("ExclusiveStructureMemberTrait")
        .shapeId(ShapeId.from("test#MultipleError"))
        .message(
          "The `alloy.proto#grpcErrorMessage` trait can be applied to only a single member " +
            "of a shape, but it was found on the following members: `detail`, `message`"
        )
        .severity(Severity.ERROR)
        .build()

    assertEquals(errorEvents.map(normalize), List(expectedError))
  }

  test("@grpcErrorMessage member must target a string member") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy.proto#grpcError
         |use alloy.proto#grpcErrorMessage
         |
         |@error("client")
         |@grpcError(code: 3)
         |structure WrongTargetError {
         |    @grpcErrorMessage
         |    code: Integer
         |}
         |""".stripMargin

    val result = Model.assembler
      .discoverModels()
      .addUnparsedModel("/test.smithy", source)
      .assemble()

    val errorEvents = result.getValidationEvents.asScala
      .filter(_.getSeverity == Severity.ERROR)
      .toList

    assert(
      errorEvents.nonEmpty,
      "Expected a validation error when @grpcErrorMessage is applied to a non-string member"
    )
  }

  test(
    "@grpcErrorMessage cannot be applied to a structure without @grpcError"
  ) {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy.proto#grpcErrorMessage
         |
         |structure NotAnError {
         |    @grpcErrorMessage
         |    message: String
         |}
         |""".stripMargin

    val result = Model.assembler
      .discoverModels()
      .addUnparsedModel("/test.smithy", source)
      .assemble()

    val errorEvents = result.getValidationEvents.asScala
      .filter(_.getSeverity == Severity.ERROR)
      .toList

    assert(
      errorEvents.nonEmpty,
      "Expected a validation error when @grpcErrorMessage is applied outside an @grpcError structure"
    )
  }
}
