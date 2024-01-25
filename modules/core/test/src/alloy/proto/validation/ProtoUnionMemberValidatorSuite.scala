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
import alloy.proto.ProtoWrappedTrait

class ProtoUnionMemberValidatorSuite extends FunSuite {

  test(
    "union - non-wrapped collection members are invalid (when shape is connected to proto-enabled shapes)"
  ) {
    val structure = StructureShape
      .builder()
      .id("com.example#Struct")
      .addTrait(new ProtoEnabledTrait())
      .addMember("foo", ShapeId.from("com.example#Foo"))
      .build()
    val list = ListShape
      .builder()
      .id("com.example#List")
      .member(ShapeId.from("shape.api#String"))
      .build()
    val map = MapShape
      .builder()
      .id("com.example#Map")
      .key(ShapeId.from("shape.api#String"))
      .value(ShapeId.from("shape.api#String"))
      .build()
    val foo = UnionShape.builder
      .id("com.example#Foo")
      .addMember("list", list.getId())
      .addMember("map", map.getId())
      .build
    val model = Model.builder
      .addShapes(structure, foo, list, map)
      .build
    val events = new ProtoUnionMemberValidator()
      .validate(model)
      .asScala
      .toList
    val listEvent = ValidationEvent
      .builder()
      .id(ProtoUnionMemberValidator.PROTO_UNION_SHAPE_HAS_UNWRAPPED_COLLECTION)
      .shapeId(ShapeId.from(s"com.example#Foo$$list"))
      .message(
        "Union members targeting collections must have the alloy.proto#protoWrapped trait"
      )
      .severity(Severity.ERROR)
      .build()
    val mapEvent = ValidationEvent
      .builder()
      .id(ProtoUnionMemberValidator.PROTO_UNION_SHAPE_HAS_UNWRAPPED_COLLECTION)
      .shapeId(ShapeId.from(s"com.example#Foo$$map"))
      .message(
        "Union members targeting collections must have the alloy.proto#protoWrapped trait"
      )
      .severity(Severity.ERROR)
      .build()
    assertEquals(events, List(listEvent, mapEvent))
  }

  // scalafmt: {maxColumn = 120}
  test(
    "union - wrapped collection members are valid (when shape is connected to proto-enabled shapes)"
  ) {
    val structure = StructureShape
      .builder()
      .id("com.example#Struct")
      .addTrait(new ProtoEnabledTrait())
      .addMember("foo", ShapeId.from("com.example#Foo"))
      .build()
    val list = ListShape
      .builder()
      .id("com.example#List")
      .member(ShapeId.from("shape.api#String"))
      .build()
    val map = MapShape
      .builder()
      .id("com.example#Map")
      .key(ShapeId.from("shape.api#String"))
      .value(ShapeId.from("shape.api#String"))
      .build()
    val foo = UnionShape.builder
      .id("com.example#Foo")
      .addMember("list", list.getId(), _.addTrait(new ProtoWrappedTrait()): Unit)
      .addMember("map", map.getId(), _.addTrait(new ProtoWrappedTrait()): Unit)
      .build
    val model = Model.builder
      .addShapes(structure, foo, list, map)
      .build
    val events = new ProtoUnionMemberValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events, List())
  }

  test(
    "union - members targeting wrapped collections are valid (when shape is connected to proto-enabled shapes)"
  ) {
    val structure = StructureShape
      .builder()
      .id("com.example#Struct")
      .addTrait(new ProtoEnabledTrait())
      .addMember("foo", ShapeId.from("com.example#Foo"))
      .build()
    val list = ListShape
      .builder()
      .id("com.example#List")
      .member(ShapeId.from("shape.api#String"))
      .addTrait(new ProtoWrappedTrait())
      .build()
    val map = MapShape
      .builder()
      .id("com.example#Map")
      .key(ShapeId.from("shape.api#String"))
      .value(ShapeId.from("shape.api#String"))
      .addTrait(new ProtoWrappedTrait())
      .build()
    val foo = UnionShape.builder
      .id("com.example#Foo")
      .addMember("list", list.getId())
      .addMember("map", map.getId())
      .build
    val model = Model.builder
      .addShapes(structure, foo, list, map)
      .build
    val events = new ProtoUnionMemberValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events, List())
  }

  test(
    "union - members targeting unwrapped collections are valid (unconnected shapes)"
  ) {
    val list = ListShape
      .builder()
      .id("com.example#List")
      .member(ShapeId.from("shape.api#String"))
      .build()
    val map = MapShape
      .builder()
      .id("com.example#Map")
      .key(ShapeId.from("shape.api#String"))
      .value(ShapeId.from("shape.api#String"))
      .build()
    val foo = UnionShape.builder
      .id("com.example#Foo")
      .addMember("list", list.getId())
      .addMember("map", map.getId())
      .build
    val model = Model.builder
      .addShapes(foo, list, map)
      .build
    val events = new ProtoUnionMemberValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events, List())
  }

}
