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
import software.amazon.smithy.model.shapes.*
import software.amazon.smithy.model.validation.Severity
import software.amazon.smithy.model.validation.ValidationEvent

import scala.jdk.CollectionConverters.*
import alloy.SimpleRestJsonTrait
import software.amazon.smithy.model.traits.TimestampFormatTrait

final class SimpleRestJsonTimestampFormatValidatorSpec extends munit.FunSuite {
  val validator = new SimpleRestJsonTimestampValidator()
  val timestamp: TimestampShape = TimestampShape
    .builder()
    .id("test#time")
    .build()

  def modelAssembler(shapes: Shape*): Model = Model.assembler().disableValidation().addShapes(shapes: _*).assemble().unwrap()

  test(
    "warn when EITHER the member targeting a timestamp or the timestamp shape itself does not have a timestamp format trait and is reachable from a service with a rest json trait"
  ) {

    val member0 = MemberShape
      .builder()
      .id("test#struct$ts")
      .target(timestamp.getId)
      .build()
    val member1 = MemberShape
      .builder()
      .id("test#struct$ts2")
      .target("smithy.api#Timestamp")
      .build()
    val struct =
      StructureShape
        .builder()
        .id("test#struct")
        .addMember(member0)
        .addMember(member1)
        .build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(new SimpleRestJsonTrait())
      .build()

    val model =
      modelAssembler(timestamp, member0, member1, struct, op, service)

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("SimpleRestJsonTimestamp")
        .severity(Severity.WARNING)
        .shape(member1)
        .message(
          "A Timestamp shape  does not have a timestamp format trait"
        )
        .build(),
      ValidationEvent
        .builder()
        .id("SimpleRestJsonTimestamp")
        .shape(member0)
        .severity(Severity.WARNING)
        .message(
          "A Timestamp shape does not have a timestamp format trait"
        )
        .build(),

    )
    assertEquals(result, expected)
  }

  test("when a timestamp shape  is not accesible from a service annotated with SimpleRestJson a warning is not issued") {

    val timestamp = TimestampShape
      .builder()
      .id("test#time")
      .build()
    val member0 = MemberShape
      .builder()
      .id("test#struct$ts")
      .target(timestamp.getId)
      .build()
    val member1 = MemberShape
      .builder()
      .id("test#struct$ts2")
      .target("smithy.api#Timestamp")
      .build()
    val struct =
      StructureShape
        .builder()
        .id("test#struct")
        .addMember(member0)
        .addMember(member1)
        .build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .build()

    val model =
      modelAssembler(timestamp, member1, member0, struct, op, service)

    val result = validator.validate(model).asScala.toList

    assertEquals(result, List.empty)
  }

  test("when a timestamp shape is annotated with the timestamp format trait a warning is not issued ") {
    val timestamp = TimestampShape
      .builder()
      .id("test#time")
      .addTrait(new TimestampFormatTrait(TimestampFormatTrait.HTTP_DATE))
      .build()
    val member0 = MemberShape
      .builder()
      .id("test#struct$ts")
      .target(timestamp.getId)
      .build()
    val struct =
      StructureShape
        .builder()
        .id("test#struct")
        .addMember(member0)
        .build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(new SimpleRestJsonTrait())
      .build()

    val model =
      modelAssembler(timestamp, member0, struct, op, service)

    val result = validator.validate(model).asScala.toList
    assertEquals(result, List.empty)
  }
  test("when a member shape that is targeting a timestamp is annotated with the timestamp format trait, a warning is not issued ") {
    val timestamp = TimestampShape
      .builder()
      .id("test#time")
      .build()
    val member0 = MemberShape
      .builder()
      .id("test#struct$ts")
      .addTrait(new TimestampFormatTrait(TimestampFormatTrait.HTTP_DATE))
      .target(timestamp.getId)
      .build()
    val member1 = MemberShape
      .builder()
      .id("test#struct$ts2")
      .addTrait(new TimestampFormatTrait(TimestampFormatTrait.HTTP_DATE))
      .target("smithy.api#Timestamp")
      .build()
    val struct =
      StructureShape
        .builder()
        .id("test#struct")
        .addMember(member0)
        .addMember(member1)
        .build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(new SimpleRestJsonTrait())
      .build()

    val model =
      modelAssembler(timestamp, member1, member0, struct, op, service)

    val result = validator.validate(model).asScala.toList
    assertEquals(result, List.empty)
  }
}
