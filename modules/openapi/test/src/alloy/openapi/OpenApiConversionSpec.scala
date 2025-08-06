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

import _root_.software.amazon.smithy.model.Model

import scala.io.Source
import scala.util.Using
import software.amazon.smithy.model.node.Node
import software.amazon.smithy.openapi.OpenApiConfig
import software.amazon.smithy.openapi.OpenApiVersion

import scala.jdk.CollectionConverters._

final class OpenApiConversionSpec extends munit.FunSuite {

  test("OpenAPI conversion from alloy#simpleRestJson protocol") {
    def runTest(version: OpenApiVersion, expectedFile: os.ResourcePath) = {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("foo.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val result = convertWithConfig(model, None, buildConfig = _ => {
      val config = new OpenApiConfig()
      config.setVersion(version)
      if(version == OpenApiVersion.VERSION_3_1_0) config.putExtensions {
        val ext = new OpenApiConfigExtension()
        ext.setEnableMultipleExamples(true)
        ext
      }
      config
    })
      .map(_.contents)
      .mkString
      .filterNot(_.isWhitespace)

    val expected = os.read(expectedFile).filterNot(_.isWhitespace)

    if (result != expected) {
      val tmp = os.pwd / "actual" / "foo.json"

      os.write.over(
        tmp,
        Node.prettyPrintJson(Node.parse(result)),
        createFolders = true
      )

      val diffText = os.proc("diff", "-u", expectedFile.toString, tmp.toString).call().out.text()

      fail(
        s"Values are not the same. Wrote current output to $tmp for easier debugging. Diff:\n$diffText"
      )
    }

    }
    runTest(OpenApiVersion.VERSION_3_0_2, os.resource / "foo.json")
    runTest(OpenApiVersion.VERSION_3_1_0, os.resource / "foo_3.1.0.json")
  }

  test(
    "OpenAPI conversion from alloy#simpleRestJson protocol with multiple namespaces"
  ) {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("foo.smithy"))
      .addImport(getClass().getClassLoader().getResource("bar.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val result = convert(model, Some(Set("bar")))
      .map(_.contents)
      .mkString
      .filterNot(_.isWhitespace)

    val expected = Using
      .resource(Source.fromResource("bar.json"))(
        _.getLines().mkString.filterNot(_.isWhitespace)
      )

    assertEquals(result, expected)
  }

  test(
    "OpenAPI conversion with one namespace excluded and one included"
  ) {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("baz.smithy"))
      .addImport(getClass().getClassLoader().getResource("bar.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val result = convert(model, Some(Set("baz")))
      .map(_.contents)
      .mkString
      .filterNot(_.isWhitespace)

    val expected = Using
      .resource(Source.fromResource("baz.json"))(
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

  test("OpenAPI conversion configuring the version") {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("foo.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val result = convertWithConfig(
      model,
      None,
      _ => {
        val config = new OpenApiConfig()
        config.setVersion(OpenApiVersion.VERSION_3_1_0)
        config
      }
    )
      .map(_.contents)
      .mkString
      .filterNot(_.isWhitespace)

    assert(result.contains("\"openapi\":\"3.1.0"))
  }

  test("OpenAPI conversion with JSON manipulating config") {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("foo.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val config = new OpenApiConfig()
    config.setJsonAdd(
      Map[String, Node](
        "/info/title" -> Node.from("Customtitlegoeshere")
      ).asJava
    )
    config.setSubstitutions(
      Map[String, Node]("X-Bamtech-Partner" -> Node.from("X-Foo")).asJava
    )

    val result = convertWithConfig(
      model,
      None,
      _ => config
    ).map(_.contents)
      .mkString
      .filterNot(_.isWhitespace)

    assert(result.contains("\"title\":\"Customtitlegoeshere\""))
    assert(!result.contains("X-Bamtech-Partner"))
    assert(result.contains("\"name\":\"X-Foo\""))
  }

  test("OpenAPI conversion of date time types") {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("datetime.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val result =
      convert(model, None).map(_.contents).mkString.filterNot(_.isWhitespace)

    val expected = List(
      """"localDate":{"type":"string","x-format":"local-date"}""",
      """"localTime":{"type":"string","x-format":"local-time"}""",
      """"localDateTime":{"type":"string","x-format":"local-date-time"}""",
      """"offsetDateTime":{"type":"string","format":"date-time","x-format":"offset-date-time"}""",
      """"offsetTime":{"type":"string","x-format":"offset-time"}""",
      """"zoneId":{"type":"string","x-format":"zone-id"}""",
      """"zoneOffset":{"type":"string","x-format":"zone-offset"}""",
      """"zonedDateTime":{"type":"string","x-format":"zoned-date-time"}""",
      """"year":{"type":"integer","format":"int32","x-format":"year"}""",
      """"yearMonth":{"type":"string","x-format":"year-month"}""",
      """"monthDay":{"type":"string","x-format":"month-day"}"""
    )

    expected.foreach { expected =>
      assert(result.contains(expected))
    }
  }
}
