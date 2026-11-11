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

import software.amazon.smithy.model.Model
import software.amazon.smithy.model.node.Node

final class MakeHeadersOptionalMapperSpec extends munit.FunSuite {

  test(
    "errors with headers on a PATCH operation convert, and the shared headers become optional"
  ) {
    val model = Model
      .assembler()
      .addImport(getClass().getClassLoader().getResource("patch.smithy"))
      .discoverModels()
      .assemble()
      .unwrap()

    val result = convert(model, None)
      .map(_.contents)
      .map(Node.parse)

    assertEquals(result.size, 1)

    val headers = result.head
      .expectObjectNode()
      .expectObjectMember("paths")
      .expectObjectMember("/things/{id}")
      .expectObjectMember("patch")
      .expectObjectMember("responses")
      .expectObjectMember("503")
      .expectObjectMember("headers")

    assertEquals(
      headers.getMembers().keySet().toString,
      "[Retry-After, X-Reason]"
    )
    assert(
      !headers
        .expectObjectMember("Retry-After")
        .getBooleanMemberOrDefault("required", false)
    )
    assert(
      !headers
        .expectObjectMember("X-Reason")
        .getBooleanMemberOrDefault("required", false)
    )
  }
}
