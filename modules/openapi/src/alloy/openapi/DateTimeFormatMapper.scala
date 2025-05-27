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
    classOf[MonthDayFormatTrait] -> "month-day",
  )
}
