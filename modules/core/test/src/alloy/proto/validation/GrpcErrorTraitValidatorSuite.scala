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

import alloy.proto.GrpcErrorTrait
import munit.FunSuite
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.validation.Severity

import scala.jdk.CollectionConverters._

class GrpcErrorTraitValidatorSuite extends FunSuite {

  test("grpcError parses numeric code") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy.proto#grpcError
         |
         |@error("client")
         |@grpcError(code: 16)
         |structure NumericError {}
         |""".stripMargin

    val model = Model.assembler
      .discoverModels()
      .addUnparsedModel("/test.smithy", source)
      .assemble()
      .unwrap()

    val code = model
      .expectShape(ShapeId.from("test#NumericError"))
      .expectTrait(classOf[GrpcErrorTrait])
      .getCode

    assertEquals(code, 16)
  }

  test("grpcError parses string symbol code") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy.proto#grpcError
         |
         |@error("client")
         |@grpcError(code: "UNAUTHENTICATED")
         |structure SymbolError {}
         |""".stripMargin

    val model = Model.assembler
      .discoverModels()
      .addUnparsedModel("/test.smithy", source)
      .assemble()
      .unwrap()

    val code = model
      .expectShape(ShapeId.from("test#SymbolError"))
      .expectTrait(classOf[GrpcErrorTrait])
      .getCode

    assertEquals(code, 16)
  }

  test("grpcError emits warning outside standard range") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy.proto#grpcError
         |
         |@error("client")
         |@grpcError(code: 42)
         |structure NonStandardError {}
         |""".stripMargin

    val model = Model.assembler
      .discoverModels()
      .addUnparsedModel("/test.smithy", source)
      .assemble()
      .unwrap()

    val events = new GrpcErrorTraitValidator()
      .validate(model)
      .asScala
      .toList

    assertEquals(events.length, 1)
    assertEquals(
      events.head.getId,
      GrpcErrorTraitValidator.GRPC_ERROR_NON_STANDARD
    )
    assertEquals(events.head.getSeverity, Severity.WARNING)
  }

  test("grpcError parses optional message") {
    val source =
      """|$version: "2"
         |
         |namespace test
         |
         |use alloy.proto#grpcError
         |
         |@error("client")
         |@grpcError(code: 16, message: "auth failed")
         |structure MessageError {}
         |""".stripMargin

    val model = Model.assembler
      .discoverModels()
      .addUnparsedModel("/test.smithy", source)
      .assemble()
      .unwrap()

    val trait_ = model
      .expectShape(ShapeId.from("test#MessageError"))
      .expectTrait(classOf[GrpcErrorTrait])

    assertEquals(trait_.getCode, 16)
    assertEquals(trait_.getMessage, java.util.Optional.of("auth failed"))
  }
}
