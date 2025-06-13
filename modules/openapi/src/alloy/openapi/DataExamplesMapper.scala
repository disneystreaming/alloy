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

package alloy.openapi

import _root_.software.amazon.smithy.jsonschema.JsonSchemaConfig
import _root_.software.amazon.smithy.jsonschema.JsonSchemaMapper
import _root_.software.amazon.smithy.jsonschema.Schema.Builder
import _root_.software.amazon.smithy.model.shapes.Shape

import scala.jdk.CollectionConverters._
import alloy.DataExamplesTrait
import software.amazon.smithy.model.node.ObjectNode
import software.amazon.smithy.model.node.Node
import software.amazon.smithy.model.node.ArrayNode

class DataExamplesMapper() extends JsonSchemaMapper {

  override def updateSchema(
      shape: Shape,
      schemaBuilder: Builder,
      config: JsonSchemaConfig
  ): Builder = if (shape.hasTrait(classOf[DataExamplesTrait])) {
    val examples = shape
      .getTrait(classOf[DataExamplesTrait])
      .get
      .getExamples()
      .asScala
      .toList
      .foldLeft(ArrayNode.builder()) { case (array, example) =>
        if (
          example.getExampleType == DataExamplesTrait.DataExampleType.STRING
        ) {
          val maybeStrNode = example.getContent().asStringNode()
          val res = if (maybeStrNode.isPresent) {
            Node.parse(maybeStrNode.get.getValue)
          } else {
            ObjectNode.builder().build()
          }
          array.withValue(res)
        } else {
          array.withValue(example.getContent())
        }
      }
      .build()
    if (examples.isEmpty()) schemaBuilder
    else schemaBuilder.putExtension("examples", examples)
  } else schemaBuilder
}
