package alloy.validation

import alloy.DiscriminatedUnionTrait
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.MemberShape
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.shapes.StructureShape
import software.amazon.smithy.model.shapes.UnionShape
import software.amazon.smithy.model.validation.Severity
import software.amazon.smithy.model.validation.ValidationEvent

import scala.jdk.CollectionConverters._

final class DiscriminatedUnionValidatorSpec extends munit.FunSuite {

  private val validator = new DiscriminatedUnionValidator()

  test("catch unions with non-structure members") {
    val member =
      MemberShape
        .builder()
        .target("smithy.api#String")
        .id("test#test$test")
        .build()
    val union = UnionShape
      .builder()
      .addTrait(new DiscriminatedUnionTrait("type"))
      .id("test#test")
      .addMember(member)
      .build()

    val model =
      Model.builder().addShape(union).build()

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("DiscriminatedUnion")
        .shape(member)
        .severity(Severity.ERROR)
        .message(
          "Target of member 'test' is not a structure shape"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test("return error when target structures contain discriminator") {
    val struct = StructureShape
      .builder()
      .id("test#struct")
      .addMember("type", ShapeId.fromParts("smithy.api", "String"))
      .build()
    val member =
      MemberShape
        .builder()
        .target("test#struct")
        .id("test#test$test")
        .build()
    val union = UnionShape
      .builder()
      .addTrait(new DiscriminatedUnionTrait("type"))
      .id("test#test")
      .addMember(member)
      .build()

    val model =
      Model.builder().addShapes(struct, union).build()

    val result = validator.validate(model).asScala.toList

    val expected = List(
      ValidationEvent
        .builder()
        .id("DiscriminatedUnion")
        .shape(member)
        .severity(Severity.ERROR)
        .message(
          "Target of member 'test' contains discriminator 'type'"
        )
        .build()
    )
    assertEquals(result, expected)
  }

  test("return no error when union contains only structure members") {
    val struct = StructureShape.builder().id("test#struct").build()
    val member =
      MemberShape
        .builder()
        .target("test#struct")
        .id("test#test$test")
        .build()
    val union = UnionShape
      .builder()
      .addTrait(new DiscriminatedUnionTrait("type"))
      .id("test#test")
      .addMember(member)
      .build()

    val model =
      Model.builder().addShapes(struct, union).build()

    val result = validator.validate(model).asScala.toList

    assertEquals(result, List.empty)
  }

}
