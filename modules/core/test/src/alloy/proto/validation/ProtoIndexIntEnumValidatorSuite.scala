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

import scala.jdk.CollectionConverters._
import alloy.proto.ProtoEnabledTrait
import software.amazon.smithy.model.validation.ValidationEvent
import software.amazon.smithy.model.validation.Severity
import alloy.OpenEnumTrait
import alloy.proto.ProtoIndexTrait

class ProtoIntEnumValidatorSuite extends FunSuite {

  test(
    "int-enum - no zero leads to errors (when shape is connected to proto-enabled shapes )"
  ) {
    val structure = StructureShape
      .builder()
      .id("com.example#Struct")
      .addTrait(new ProtoEnabledTrait())
      .addMember("foo", ShapeId.from("com.example#Foo"))
      .build()
    val foo = IntEnumShape.builder
      .id("com.example#Foo")
      .addMember("bar", 1)
      .build
    val model = Model.builder
      .addShapes(structure, foo)
      .build
    val events = new ProtoIntEnumValidator()
      .validate(model)
      .asScala
      .toList
    val event = ValidationEvent
      .builder()
      .id(ProtoIntEnumValidator.PROTO_INT_ENUM_HAS_NO_ZERO)
      .shapeId(foo)
      .message(
        "intEnum shape must have a 0 value when connected to a shape that has the protoEnabled trait"
      )
      .severity(Severity.ERROR)
      .build()
    assertEquals(events, List(event))
  }

  test("int-enum - no-zero on open enums is valid") {
    val structure = StructureShape
      .builder()
      .id("com.example#Struct")
      .addTrait(new ProtoEnabledTrait())
      .addMember("foo", ShapeId.from("com.example#Foo"))
      .build()
    val foo = IntEnumShape.builder
      .id("com.example#Foo")
      .addMember("bar", 1)
      .addTrait(new OpenEnumTrait())
      .build
    val model = Model.builder
      .addShapes(structure, foo)
      .build
    val events = new ProtoIntEnumValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events, Nil)
  }

  test(
    "int-enum - no-zero on open enums is valid if protoIndex(0) is present"
  ) {
    val structure = StructureShape
      .builder()
      .id("com.example#Struct")
      .addTrait(new ProtoEnabledTrait())
      .addMember("foo", ShapeId.from("com.example#Foo"))
      .build()
    val foo = IntEnumShape.builder
      .id("com.example#Foo")
      .addMember("bar", 1, _.addTrait(new ProtoIndexTrait(0)): Unit)
      .build
    val model = Model.builder
      .addShapes(structure, foo)
      .build
    val events = new ProtoIntEnumValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events, Nil)
  }

  test(
    "int-enum - no-zero on open enums is valid if shape is not connected to proto-enabled shape"
  ) {
    val foo = IntEnumShape.builder
      .id("com.example#Foo")
      .addMember("bar", 1)
      .build
    val model = Model.builder
      .addShapes(foo)
      .build
    val events = new ProtoIntEnumValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events, Nil)
  }

}
