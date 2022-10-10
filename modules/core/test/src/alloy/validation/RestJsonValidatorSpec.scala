package alloy.validation

import alloy.RestJsonTrait
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.pattern.UriPattern
import software.amazon.smithy.model.shapes._
import software.amazon.smithy.model.traits.HttpTrait
import software.amazon.smithy.model.validation._

import scala.jdk.CollectionConverters._

final class RestJsonValidationSpec extends munit.FunSuite {

  private def validator = new RestJsonValidator()

  test(
    "validation events are returned when operations are missing http trait"
  ) {
    val op = OperationShape
      .builder()
      .id("test#op")
      .input(StructureShape.builder().id("test#struct").build())
      .build()

    val service = ServiceShape
      .builder()
      .id("test#serv")
      .version("1")
      .addTrait(new RestJsonTrait())
      .addOperation(op)
      .build()

    val model = Model.builder().addShape(service).addShape(op).build()

    val result = validator.validate(model).asScala.toList
    val expected = List(
      ValidationEvent
        .builder()
        .id("RestJson")
        .shape(op)
        .severity(Severity.ERROR)
        .message(
          "Operations tied to alloy#restJson services must be annotated with the @http trait"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test(
    "no events are returned when operations have http trait"
  ) {

    val httpTrait = HttpTrait
      .builder()
      .code(200)
      .method("POST")
      .uri(UriPattern.parse("/test"))
      .build()
    val op = OperationShape
      .builder()
      .id("test#op")
      .input(StructureShape.builder().id("test#struct").build())
      .addTrait(httpTrait)
      .build()

    val service = ServiceShape
      .builder()
      .id("test#serv")
      .version("1")
      .addTrait(new RestJsonTrait())
      .addOperation(op)
      .build()

    val model = Model.builder().addShape(service).addShape(op).build()

    val result = validator.validate(model).asScala.toList
    val expected = List.empty
    assertEquals(result, expected)
  }

  test(
    "validation events are not returned when service is not restJson"
  ) {
    val op = OperationShape
      .builder()
      .id("test#op")
      .input(StructureShape.builder().id("test#struct").build())
      .build()

    val service = ServiceShape
      .builder()
      .id("test#serv")
      .version("1")
      .addOperation(op)
      .build()

    val model = Model.builder().addShape(service).addShape(op).build()

    val result = validator.validate(model).asScala.toList
    val expected = List.empty
    assertEquals(result, expected)
  }

  test(
    "Validator is wired to the jvm Service mechanism"
  ) {
    val modelString =
      """|namespace foo
         |
         |use alloy#restJson
         |
         |@restJson
         |service HelloService {
         |  version : "1",
         |  operations : [Greet]
         |}
         |
         |operation Greet {
         |}
         |
         |""".stripMargin

    val events = Model
      .assembler(this.getClass().getClassLoader())
      .discoverModels()
      .addUnparsedModel("foo.smithy", modelString)
      .assemble()
      .getValidationEvents()
      .asScala
      .filter(_.getSeverity() == Severity.ERROR)
      .toList

    assert(events.size == 1)
  }

}
