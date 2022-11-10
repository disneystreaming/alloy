

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
import software.amazon.smithy.model.validation.Severity
import software.amazon.smithy.model.validation.ValidationEvent

import scala.jdk.CollectionConverters._
import alloy.SimpleRestJsonTrait

final class SimpleRestJsonTimestampFormatValidatorSpec extends munit.FunSuite {

    test("warn when timestamp shape does not have a timestamp format trait and is reachable from a service with a rest json trait") {
        val validator = new SimpleRestJsonTimestampValidator()
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
                StructureShape.builder().id("test#struct").addMember(member0).addMember(member1).build()

        val op = OperationShape.builder().id("test#TestOp").input(struct).build()
        val service = ServiceShape
                .builder()
                .id("test#TestService")
                .version("1")
                .addOperation(op)
                .addTrait(new SimpleRestJsonTrait())
                .build()

        val model =
                Model.builder().addShapes(timestamp,member1,struct, op, service).build()


        val result = validator.validate(model).asScala.toList

        val expected = List(
                ValidationEvent
                        .builder()
                        .id("SimpleRestJsonTimestamp")
                        .severity(Severity.WARNING)
                        .message(
                                "test#time: Timestamp shape test#time does not have a timestamp format trait "
                        )
                        .build()
        )
        assertEquals(result.size, 2)
    }

}
