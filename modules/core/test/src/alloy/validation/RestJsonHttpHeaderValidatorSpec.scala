package alloy.validation

import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes._
import software.amazon.smithy.model.traits.HttpHeaderTrait
import software.amazon.smithy.model.validation.Severity
import software.amazon.smithy.model.validation.ValidationEvent
import software.amazon.smithy.aws.traits.protocols.RestJson1Trait

import scala.jdk.CollectionConverters._
import alloy.SimpleRestJsonTrait

final class SimpleRestJsonHttpHeaderValidatorSpec extends munit.FunSuite {

  test("reject models with content-type header") {
    val validator = new SimpleRestJsonHttpHeaderValidator()
    val member = MemberShape
      .builder()
      .id("test#struct$testing")
      .target("smithy.api#String")
      .addTrait(new HttpHeaderTrait("Content-Type"))
      .build()
    val struct =
      StructureShape.builder().id("test#struct").addMember(member).build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(new SimpleRestJsonTrait())
      .build()

    val model =
      Model.builder().addShapes(struct, op, service).build()

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("SimpleRestJsonHttpHeader")
        .shape(member)
        .severity(Severity.WARNING)
        .message(
          "Header named `Content-Type` may be overridden in client/server implementations"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test("accept random arbitrary header") {
    val validator = new SimpleRestJsonHttpHeaderValidator()
    val member = MemberShape
      .builder()
      .id("test#struct$testing")
      .target("smithy.api#String")
      .addTrait(new HttpHeaderTrait("random"))
      .build()
    val struct =
      StructureShape.builder().id("test#struct").addMember(member).build()

    val model =
      Model.builder().addShape(struct).build()

    val result = validator.validate(model).asScala.toList

    assertEquals(result, List.empty)
  }

  test("accept other header in closure of rest-json service") {
    val validator = new SimpleRestJsonHttpHeaderValidator()
    val member = MemberShape
      .builder()
      .id("test#struct$testing")
      .target("smithy.api#String")
      .addTrait(new HttpHeaderTrait("Some-Header"))
      .build()
    val struct =
      StructureShape.builder().id("test#struct").addMember(member).build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(new SimpleRestJsonTrait())
      .build()

    val model =
      Model.builder().addShapes(struct, op, service).build()

    val result = validator.validate(model).asScala.toList
    assertEquals(result, List.empty)
  }

  test("accept content-type header as part of different protocol") {
    val validator = new SimpleRestJsonHttpHeaderValidator()
    val member = MemberShape
      .builder()
      .id("test#struct$testing")
      .target("smithy.api#String")
      .addTrait(new HttpHeaderTrait("Content-Type"))
      .build()
    val struct =
      StructureShape.builder().id("test#struct").addMember(member).build()

    val op = OperationShape.builder().id("test#TestOp").input(struct).build()
    val service = ServiceShape
      .builder()
      .id("test#TestService")
      .version("1")
      .addOperation(op)
      .addTrait(RestJson1Trait.builder().build())
      .build()

    val model =
      Model.builder().addShapes(struct, op, service).build()

    val result = validator.validate(model).asScala.toList
    assertEquals(result, List.empty)
  }

}
