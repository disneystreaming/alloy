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
import software.amazon.smithy.model.shapes._
import alloy.proto.ProtoIndexTrait

import scala.jdk.CollectionConverters._
import scala.annotation.nowarn

class ProtoIndexTraitValidatorSuite extends FunSuite {

  val string = StringShape.builder.id("com.example#String").build
  val int = IntegerShape.builder.id("com.example#Integer").build

  test("well-formed field numbers") {
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .addMember(
        "baz",
        int.getId,
        _.addTrait(new ProtoIndexTrait(2)): @nowarn
      )
      .build
    val model = Model.builder
      .addShapes(string, int, foo)
      .build
    val events = new ProtoIndexTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 0)
  }

  test("duplicate field numbers in structure are invalid") {
    val foo = StructureShape.builder
      .id("com.example#Foo1")
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .addMember(
        "baz",
        int.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .build
    val model = Model.builder
      .addShapes(string, int, foo)
      .build
    val events = new ProtoIndexTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getEventId(),
      ProtoIndexTraitValidator.DUPLICATED_PROTO_INDEX
    )
  }

  test("inconsistent field numbers in structure are invalid") {
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .addMember("baz", int.getId)
      .build
    val model = Model.builder
      .addShapes(string, int, foo)
      .build
    val events = new ProtoIndexTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getEventId(),
      ProtoIndexTraitValidator.INCONSISTENT_PROTO_INDEXES
    )
  }

}
