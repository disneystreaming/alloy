package alloy.openapi

import _root_.software.amazon.smithy.model.Model

import scala.io.Source
import scala.util.Using

final class OpenApiConversionSpec extends munit.FunSuite {

  test("OpenAPI conversion from alloy#restJson protocol") {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("foo.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val result = convert(model, None)
      .map(_.contents)
      .mkString
      .filterNot(_.isWhitespace)

    val expected = Using
      .resource(Source.fromResource("foo.json"))(
        _.getLines().mkString.filterNot(_.isWhitespace)
      )

    assertEquals(result, expected)
  }

  test("OpenAPI conversion from testJson protocol") {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("baz.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val result = convert(model, None)
      .map(_.contents)
      .mkString
      .filterNot(_.isWhitespace)

    val expected = Using
      .resource(Source.fromResource("baz.json"))(
        _.getLines().mkString.filterNot(_.isWhitespace)
      )
    assertEquals(result, expected)
  }

}
