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

package alloy.validation

import software.amazon.smithy.model.Model

import scala.jdk.CollectionConverters._
import software.amazon.smithy.model.validation.Severity

final class HttpPolymorphicResponseValidatorSpec extends munit.FunSuite {

  test(
    "Validator checks that all targets of the polymorphic response union have @httpSuccess"
  ) {
    val modelString =
      """|$version: "2"
         |
         |namespace foo
         |
         |use alloy#httpPolymorphicResponse
         |
         |operation Test {
         |  output := {
         |    @required
         |    @httpPolymorphicResponse
         |    response: Response
         |  }
         |}
         |
         |union Response {
         |  created: Created
         |}
         |
         |structure Created {
         |}
         |
         |""".stripMargin

    val events = Model
      .assembler(this.getClass().getClassLoader())
      .addUnparsedModel("foo.smithy", modelString)
      .discoverModels()
      .assemble()
      .getValidationEvents()
      .asScala
      .filter(_.getSeverity() == Severity.ERROR)

    assertEquals(events.size, 1)
    assertEquals(
      events.head.getMessage(),
      HttpPolymorphicResponseValidator.EXPECTED_HTTP_SUCCESS_ON_ALL_MEMBER_TARGETS
    )
  }

  test(
    "Validator checks that @httpPolymorphicResponse annotates the only member of a structure"
  ) {
    val modelString =
      """|$version: "2"
         |
         |namespace foo
         |
         |use alloy#httpPolymorphicResponse
         |use alloy#httpSuccess
         |
         |operation Test {
         |  output := {
         |    @required
         |    @httpPolymorphicResponse
         |    response: Response
         |
         |    illegalMember: String
         |  }
         |}
         |
         |union Response {
         |  created: Created
         |}
         |
         |
         |@httpSuccess(201)
         |structure Created {
         |}
         |
         |""".stripMargin

    val events = Model
      .assembler(this.getClass().getClassLoader())
      .addUnparsedModel("foo.smithy", modelString)
      .discoverModels()
      .assemble()
      .getValidationEvents()
      .asScala
      .filter(_.getSeverity() == Severity.ERROR)

    assertEquals(events.size, 1)
    assertEquals(
      events.head.getMessage(),
      HttpPolymorphicResponseValidator.EXPECTED_SINGLE_MEMBER
    )
  }

  test(
    "Validator checks that union-members target shapes with distinct @httpSuccess values"
  ) {
    val modelString =
      """|$version: "2"
         |
         |namespace foo
         |
         |use alloy#httpPolymorphicResponse
         |use alloy#httpSuccess
         |
         |operation Test {
         |  output := {
         |    @required
         |    @httpPolymorphicResponse
         |    response: Response
         |  }
         |}
         |
         |union Response {
         |  created: Created
         |  okay: Okay
         |}
         |
         |
         |@httpSuccess(201)
         |structure Created {
         |}
         |
         |@httpSuccess(201)
         |structure Okay {
         |}
         |
         |""".stripMargin

    val events = Model
      .assembler(this.getClass().getClassLoader())
      .addUnparsedModel("foo.smithy", modelString)
      .discoverModels()
      .assemble()
      .getValidationEvents()
      .asScala
      .filter(_.getSeverity() == Severity.ERROR)

    assertEquals(events.size, 1)
    assertEquals(
      events.head.getMessage(),
      HttpPolymorphicResponseValidator.EXPECTED_DISTINCT_HTTP_SUCCESS
    )
  }

}
