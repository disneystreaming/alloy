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

import alloy.proto.ProtoInlinedOneOfTrait;
import munit.FunSuite
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes._

import scala.jdk.CollectionConverters._

class ProtoInlinedOneOfValidatorSuite extends FunSuite {

  val req = StructureShape.builder.id("com.example#Request").build
  val res = StructureShape.builder.id("com.example#Response").build
  val error = StructureShape.builder.id("com.example#Error").build

  test("union annotated is used exactly once") {
    val union = UnionShape.builder
      .id("com.example#MyUnion")
      .addTrait(new ProtoInlinedOneOfTrait())
      .build
    val structure = StructureShape.builder
      .id("com.example#MyStructure")
      .addMember("myUnion", union.getId())
      .build

    val model = Model.builder.addShapes(structure, union).build
    val events = new ProtoInlinedOneOfValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 0)
  }

  test("union annotated must be used") {
    val union = UnionShape.builder
      .id("com.example#MyUnion")
      .addTrait(new ProtoInlinedOneOfTrait())
      .build

    val model = Model.builder.addShapes(union).build
    val events = new ProtoInlinedOneOfValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getEventId(),
      ProtoInlinedOneOfValidator.UNUSED_UNION
    )
  }

  test("union annotated is used more than once") {
    val union = UnionShape.builder
      .id("com.example#MyUnion")
      .addTrait(new ProtoInlinedOneOfTrait())
      .build
    val structure = StructureShape.builder
      .id("com.example#MyStructure")
      .addMember("myUnion", union.getId())
      .build
    val structure2 = StructureShape.builder
      .id("com.example#OtherStructure")
      .addMember("myUnion", union.getId())
      .build

    val model = Model.builder.addShapes(structure, structure2, union).build
    val events = new ProtoInlinedOneOfValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getEventId(),
      ProtoInlinedOneOfValidator.USAGE_COUNT_EXCEEDED
    )
  }

}
