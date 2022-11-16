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

  test("structure - well-formed field numbers") {
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

  test("structure - duplicate field numbers in structure are invalid") {
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
      .map(_.getId)
      .toList
    assertEquals(
      events,
      List(ProtoIndexTraitValidator.DUPLICATED_PROTO_INDEX)
    )
  }

  test("structure - inconsistent field numbers in structure are invalid") {
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
      .map(_.getId)
      .toList
    assertEquals(
      events,
      List(ProtoIndexTraitValidator.INCONSISTENT_PROTO_INDEXES)
    )
  }

  test("structure - consistent numbers in member's union are valid") {
    val union = UnionShape
      .builder()
      .id("com.example#Union")
      .addMember(
        MemberShape
          .builder()
          .id("com.example#Union$age")
          .target("smithy.api#Integer")
          .addTrait(new ProtoIndexTrait(3))
          .build()
      )
      .addMember(
        MemberShape
          .builder()
          .id("com.example#Union$name")
          .target("smithy.api#String")
          .addTrait(new ProtoIndexTrait(4))
          .build()
      )
      .build()
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .addMember(
        "union",
        union.getId
      )
      .build
    val model = Model.builder
      .addShapes(string, int, foo, union)
      .build
    val events = new ProtoIndexTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events, List.empty)
  }

  test("structure - duplicated numbers in member's union are invalid") {
    val union = UnionShape
      .builder()
      .id("com.example#Union")
      .addMember(
        MemberShape
          .builder()
          .id("com.example#Union$age")
          .target("smithy.api#Integer")
          .addTrait(new ProtoIndexTrait(1))
          .build()
      )
      .addMember(
        MemberShape
          .builder()
          .id("com.example#Union$name")
          .target("smithy.api#String")
          .addTrait(new ProtoIndexTrait(2))
          .build()
      )
      .build()
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .addMember(
        "union",
        union.getId
      )
      .build
    val model = Model.builder
      .addShapes(string, int, foo, union)
      .build
    val events = new ProtoIndexTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(
      events.head.getId,
      ProtoIndexTraitValidator.DUPLICATED_PROTO_INDEX
    )
  }

  test("structure - inconsistent numbers in member's union are invalid") {
    val union = UnionShape
      .builder()
      .id("com.example#Union")
      .addMember(
        MemberShape
          .builder()
          .id("com.example#Union$age")
          .target("smithy.api#Integer")
          .build()
      )
      .addMember(
        MemberShape
          .builder()
          .id("com.example#Union$name")
          .target("smithy.api#String")
          .addTrait(new ProtoIndexTrait(2))
          .build()
      )
      .build()
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn(
          "msg=discarded non-Unit value"
        )
      )
      .addMember(
        "union",
        union.getId
      )
      .build
    val model = Model.builder
      .addShapes(string, int, foo, union)
      .build
    val events = new ProtoIndexTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(
      events.head.getId,
      ProtoIndexTraitValidator.INCONSISTENT_PROTO_INDEXES
    )
  }

  test("structure - ensure union member are proto indexed, not just the structure member referencing the union") {
    val union = UnionShape
      .builder()
      .id("com.example#Union")
      .addMember(
        MemberShape
          .builder()
          .id("com.example#Union$bar") // same name to trigger a conflict
          .target("smithy.api#Integer")
          .build()
      )
      .addMember(
        MemberShape
          .builder()
          .id("com.example#Union$name")
          .target("smithy.api#String")
          .build()
      )
      .build()
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .addMember(
        "union",
        union.getId,
        _.addTrait(new ProtoIndexTrait(2)): @nowarn(
          "msg=discarded non-Unit value"
        )
      )
      .build
    val model = Model.builder
      .addShapes(string, int, foo, union)
      .build
    val events = new ProtoIndexTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(
      events.head.getId,
      ProtoIndexTraitValidator.INCONSISTENT_PROTO_INDEXES
    )
  }
}
