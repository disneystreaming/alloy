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
import alloy.proto.GrpcTrait

class GrpcTraitValidatorSuite extends FunSuite {

  val req = StructureShape.builder.id("com.example#Request").build
  val res = StructureShape.builder.id("com.example#Response").build
  val error = StructureShape.builder.id("com.example#Error").build

  test("well-formed service") {
    val op = OperationShape.builder
      .id("com.example#Operation")
      .input(req)
      .output(res)
      .build
    val service = ServiceShape.builder
      .id("com.example#Service")
      .addOperation(op)
      .addTrait(new GrpcTrait)
      .build
    val model = Model.builder.addShapes(req, res, op, service).build
    val events = new GrpcTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 0)
  }

  test("service w/o operations") {
    val service = ServiceShape.builder
      .id("com.example#Service")
      .addTrait(new GrpcTrait)
      .build
    val model = Model.builder.addShapes(req, res, service).build
    val events = new GrpcTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getId(),
      GrpcTraitValidator.NO_OPERATION_SPECIFIED
    )
  }

  test("operation without input") {
    val op = OperationShape.builder
      .id("com.example#Operation")
      .output(res)
      .build
    val service = ServiceShape.builder
      .id("com.example#Service")
      .addOperation(op)
      .addTrait(new GrpcTrait)
      .build
    val model = Model.builder.addShapes(req, res, op, service).build
    val events = new GrpcTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getId(),
      GrpcTraitValidator.NO_INPUT_SHAPE_SPECIFIED
    )
  }

  test("operation without output") {
    val op = OperationShape.builder
      .id("com.example#Operation")
      .input(req)
      .build
    val service = ServiceShape.builder
      .id("com.example#Service")
      .addOperation(op)
      .addTrait(new GrpcTrait)
      .build
    val model = Model.builder.addShapes(req, res, op, service).build
    val events = new GrpcTraitValidator()
      .validate(model)
      .asScala
      .toList
    assertEquals(events.length, 1)
    assertEquals(
      events(0).getId(),
      GrpcTraitValidator.NO_OUTPUT_SHAPE_SPECIFIED
    )
  }

}
