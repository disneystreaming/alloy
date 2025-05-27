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
package openapi

import software.amazon.smithy.jsonschema.JsonSchemaMapper
import software.amazon.smithy.jsonschema.JsonSchemaMapperContext
import software.amazon.smithy.jsonschema.Schema
import software.amazon.smithy.model.node.Node
import software.amazon.smithy.model.traits.Trait

class DateTimeFormatMapper() extends JsonSchemaMapper {

  override def updateSchema(
      context: JsonSchemaMapperContext,
      schemaBuilder: Schema.Builder
  ): Schema.Builder = {
    val shape = context.getShape()
    DateTimeFormatMapper.mapping
      .collectFirst { case (tr, format) if shape.hasTrait(tr) => format }
      .map { format =>
        schemaBuilder.putExtension("x-format", Node.from(format))
      }
      .getOrElse(schemaBuilder)
  }
}

object DateTimeFormatMapper {
  private val mapping: Map[Class[_ <: Trait], String] = Map(
    classOf[DateFormatTrait] -> "local-date",
    classOf[LocalTimeFormatTrait] -> "local-time",
    classOf[LocalDateTimeFormatTrait] -> "local-date-time",
    classOf[OffsetDateTimeFormatTrait] -> "offset-date-time",
    classOf[OffsetTimeFormatTrait] -> "offset-time",
    classOf[ZoneIdFormatTrait] -> "zone-id",
    classOf[ZoneOffsetFormatTrait] -> "zone-offset",
    classOf[ZonedDateTimeFormatTrait] -> "zoned-date-time",
    classOf[YearFormatTrait] -> "year",
    classOf[YearMonthFormatTrait] -> "year-month",
    classOf[MonthDayFormatTrait] -> "month-day"
  )
}
