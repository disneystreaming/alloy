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

import software.amazon.smithy.model.shapes.ListShape
import software.amazon.smithy.model.shapes.MemberShape
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.shapes.StringShape
import software.amazon.smithy.model.shapes.StructureShape
import software.amazon.smithy.model.Model
import java.util.Optional

final class UrlFormFlattenedTraitProviderSpec extends munit.FunSuite {

  test("has trait") {
    val stringShape = StringShape
      .builder()
      .id(ShapeId.fromParts("test", "MyString"))
      .build()
    val listShape = ListShape
      .builder()
      .id(ShapeId.fromParts("test", "MyList"))
      .member(stringShape.getId)
      .build()
    val structId = ShapeId.fromParts("test", "MyStruct")
    val targetId = structId.withMember("myList")
    val structShape = StructureShape
      .builder()
      .id(structId)
      .addMember(
        MemberShape
          .builder()
          .id(targetId)
          .target(listShape.getId)
          .addTrait(new UrlFormFlattenedTrait)
          .build()
      )
      .build()

    val model =
      Model.assembler.disableValidation
        .addShapes(structShape, listShape, stringShape)
        .assemble()
        .unwrap()

    val result = model
      .getShape(targetId)
      .map(shape => shape.hasTrait(classOf[UrlFormFlattenedTrait]))

    assertEquals(result, Optional.of(true))
  }
}
