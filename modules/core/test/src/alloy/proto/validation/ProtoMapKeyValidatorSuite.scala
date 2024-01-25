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

class ProtoMapKeyValidatorSuite extends FunSuite {

  test(
    "union - non-wrapped collection members are invalid (when shape is connected to proto-enabled shapes)"
  ) {
    val string = StringShape.builder().id("com.example#String").build()
    val structure = StructureShape
      .builder()
      .id("com.example#Struct")
      .addTrait(new ProtoEnabledTrait())
      .addMember("foo", ShapeId.from("com.example#Map"))
      .build()
    val map = MapShape
      .builder()
      .id("com.example#Map")
      .key(
        string.getId(),
        _.addTrait(new ProtoWrappedTrait()): Unit
      )
      .value(string.getId())
      .build()
    val model = Model.builder
      .addShapes(structure, map, string)
      .build
    val events = new ProtoMapKeyValidator()
      .validate(model)
      .asScala
      .toList
    val mapEvent = ValidationEvent
      .builder()
      .id(ProtoMapKeyValidator.PROTO_INVALID_MAP_KEY)
      .shapeId(ShapeId.from(s"com.example#Map$$key"))
      .message(
        "Map keys must not have the alloy.proto#protoWrapped trait, nor target shapes with that trait"
      )
      .severity(Severity.ERROR)
      .build()
    assertEquals(events, List(mapEvent))
  }

}
