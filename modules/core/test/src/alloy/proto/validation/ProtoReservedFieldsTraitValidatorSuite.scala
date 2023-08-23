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
import alloy.proto.{
  ProtoReservedFieldsTrait,
  ProtoIndexTrait,
  ProtoReservedFieldsTraitValue
}

import scala.jdk.CollectionConverters._
import scala.annotation.nowarn

class ProtoReservedFieldsTraitValidatorSuite extends FunSuite {

  val string = StringShape.builder.id("com.example#String").build

  test("well-formed structure") {
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addTrait(
        ProtoReservedFieldsTrait
          .builder()
          .add(ProtoReservedFieldsTraitValue.builder().number(3).build())
          .build()
      )
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .build
    val model = Model.builder
      .addShapes(string, foo)
      .build
    val events = new ProtoReservedFieldsTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 0)
  }

  test("reserved field number") {
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addTrait(
        ProtoReservedFieldsTrait
          .builder()
          .add(ProtoReservedFieldsTraitValue.builder().number(1).build())
          .build()
      )
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .build
    val model = Model.builder
      .addShapes(string, foo)
      .build
    val events = new ProtoReservedFieldsTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getId(),
      ProtoReservedFieldsTraitValidator.RESERVED_NUMBER_IN_STRUCTURE
    )
  }

  test("reserved field name") {
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addTrait(
        ProtoReservedFieldsTrait
          .builder()
          .add(ProtoReservedFieldsTraitValue.builder().name("bar").build())
          .build()
      )
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(1)): @nowarn
      )
      .build
    val model = Model.builder
      .addShapes(string, foo)
      .build
    val events = new ProtoReservedFieldsTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getId(),
      ProtoReservedFieldsTraitValidator.RESERVED_NAME_IN_STRUCTURE
    )
  }

  test("reserved field range") {
    val foo = StructureShape.builder
      .id("com.example#Foo")
      .addTrait(
        ProtoReservedFieldsTrait
          .builder()
          .add(
            ProtoReservedFieldsTraitValue
              .builder()
              .range(new ProtoReservedFieldsTraitValue.Range(1, 10))
              .build()
          )
          .build()
      )
      .addMember(
        "bar",
        string.getId,
        _.addTrait(new ProtoIndexTrait(2)): @nowarn
      )
      .build
    val model = Model.builder
      .addShapes(string, foo)
      .build
    val events = new ProtoReservedFieldsTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getId(),
      ProtoReservedFieldsTraitValidator.RESERVED_NUMBER_IN_STRUCTURE
    )
  }

}
