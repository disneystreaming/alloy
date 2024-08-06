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

package alloy

import software.amazon.smithy.model.shapes.MemberShape
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.shapes.DocumentShape
import software.amazon.smithy.model.shapes.StructureShape
import software.amazon.smithy.model.Model
import java.util.Optional

final class JsonUnknownTraitProviderSpec extends munit.FunSuite {

  test("has trait") {
    val documentShape = DocumentShape
      .builder()
      .id(ShapeId.fromParts("test", "MyDocument"))
      .build()
    val structId = ShapeId.fromParts("test", "MyStruct")
    val targetId = structId.withMember("myMap")
    val structShape = StructureShape
      .builder()
      .id(structId)
      .addMember(
        MemberShape
          .builder()
          .id(targetId)
          .target(documentShape.getId)
          .addTrait(new JsonUnknownTrait)
          .build()
      )
      .build()

    val model =
      Model.assembler
        .addShapes(structShape, documentShape)
        .assemble()
        .unwrap()

    val result = model
      .getShape(targetId)
      .map(shape => shape.hasTrait(classOf[JsonUnknownTrait]))

    assertEquals(result, Optional.of(true))
  }
}
