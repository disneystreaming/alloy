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

import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes._
import software.amazon.smithy.model.traits.HttpHeaderTrait
import software.amazon.smithy.model.validation.Severity
import software.amazon.smithy.model.validation.ValidationEvent
import software.amazon.smithy.aws.traits.protocols.RestJson1Trait

import scala.jdk.CollectionConverters._
import alloy.SimpleRestJsonTrait

final class SimpleRestJsonHttpHeaderValidatorSpec extends munit.FunSuite {

  test("reject models with content-type header") {
    val validator = new SimpleRestJsonHttpHeaderValidator()
    val member = MemberShape
      .builder()
      .id("test#struct$testing")
      .target("smithy.api#String")
      .addTrait(new HttpHeaderTrait("Content-Type"))
      .build()
    val struct =
      StructureShape.builder().id("test#struct").addMember(member).build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(new SimpleRestJsonTrait())
      .build()

    val model =
      Model.builder().addShapes(struct, op, service).build()

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("SimpleRestJsonHttpHeader")
        .shape(member)
        .severity(Severity.WARNING)
        .message(
          "Header named `Content-Type` may be overridden in client/server implementations"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test("accept random arbitrary header") {
    val validator = new SimpleRestJsonHttpHeaderValidator()
    val member = MemberShape
      .builder()
      .id("test#struct$testing")
      .target("smithy.api#String")
      .addTrait(new HttpHeaderTrait("random"))
      .build()
    val struct =
      StructureShape.builder().id("test#struct").addMember(member).build()

    val model =
      Model.builder().addShape(struct).build()

    val result = validator.validate(model).asScala.toList

    assertEquals(result, List.empty)
  }

  test("accept other header in closure of rest-json service") {
    val validator = new SimpleRestJsonHttpHeaderValidator()
    val member = MemberShape
      .builder()
      .id("test#struct$testing")
      .target("smithy.api#String")
      .addTrait(new HttpHeaderTrait("Some-Header"))
      .build()
    val struct =
      StructureShape.builder().id("test#struct").addMember(member).build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(new SimpleRestJsonTrait())
      .build()

    val model =
      Model.builder().addShapes(struct, op, service).build()

    val result = validator.validate(model).asScala.toList
    assertEquals(result, List.empty)
  }

  test("accept content-type header as part of different protocol") {
    val validator = new SimpleRestJsonHttpHeaderValidator()
    val member = MemberShape
      .builder()
      .id("test#struct$testing")
      .target("smithy.api#String")
      .addTrait(new HttpHeaderTrait("Content-Type"))
      .build()
    val struct =
      StructureShape.builder().id("test#struct").addMember(member).build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(RestJson1Trait.builder().build())
      .build()

    val model =
      Model.builder().addShapes(struct, op, service).build()

    val result = validator.validate(model).asScala.toList
    assertEquals(result, List.empty)
  }

}
