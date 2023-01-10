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

import munit.Location
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.traits.Trait
import software.amazon.smithy.model.validation.ValidatedResult

import scala.io.Source

final class SanitySpec extends munit.FunSuite {

  private val model: ValidatedResult[Model] =
    Model.assembler().discoverModels().assemble()

  test("traits provider are defined and work") {
    val unwrapped = model.unwrap()
    def traitLookup[T <: Trait](c: Class[T])(implicit loc: Location): Unit = {
      val shapeSet = unwrapped.getShapesWithTrait(c)
      assert(
        !shapeSet.isEmpty(),
        s"Found ${shapeSet.size()} shapes with ${c.getSimpleName()} trait."
      )
    }

    val lines = scala.util
      .Using(
        Source.fromResource(
          "META-INF/services/software.amazon.smithy.model.traits.TraitService"
        )
      ) {
        _.getLines().toList
      }
      .fold(
        ex => fail("Failed to load TraitService resource", ex),
        identity
      )
    val classesFQN = lines.map(_.split("\\$").head)
    val classes =
      classesFQN.map(name => this.getClass().getClassLoader().loadClass(name))
    assert(
      classes.size > 10,
      s"Loading Trait classes probably failed, only found ${classes.size} classes."
    )
    classes.map(c => traitLookup(c.asInstanceOf[Class[Trait]]))
  }
}
