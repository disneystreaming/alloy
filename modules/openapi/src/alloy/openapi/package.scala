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

import software.amazon.smithy.model.Model
import software.amazon.smithy.model.node.Node
import software.amazon.smithy.model.shapes.Shape
import software.amazon.smithy.model.shapes.ShapeId
import software.amazon.smithy.model.traits.Trait
import software.amazon.smithy.openapi.OpenApiConfig
import software.amazon.smithy.openapi.fromsmithy.OpenApiConverter
import software.amazon.smithy.openapi.fromsmithy.Smithy2OpenApiExtension

import java.util.ServiceLoader
import scala.jdk.CollectionConverters._

package object openapi {

  def convertWithConfig(
      model: Model,
      service: Shape,
      buildConfig: Unit => OpenApiConfig,
      classLoader: ClassLoader
  ): List[OpenApiConversionResult] = {

    import scala.jdk.CollectionConverters._

    case class TraitKey[T <: Trait](cls: Class[T]) {
      def getIdIfApplied(shape: Shape): Option[ShapeId] = {
        val maybeTrait = shape.getTrait(cls)
        if (maybeTrait.isPresent()) {
          Some(maybeTrait.get().toShapeId())
        } else None
      }
    }
    val openapiAwareTraits: Set[TraitKey[_]] = ServiceLoader
      .load(
        classOf[Smithy2OpenApiExtension],
        classLoader
      )
      .asScala
      .toVector
      .flatMap(_.getProtocols().asScala.map(p => TraitKey(p.getProtocolType())))
      .toSet

    val protocols: Set[ShapeId] =
      openapiAwareTraits.flatMap(_.getIdIfApplied(service))

    protocols.map { protocol =>
      val serviceId = service.getId()
      val config = buildConfig(())
      config.setService(serviceId)
      config.setProtocol(protocol)
      config.setIgnoreUnsupportedTraits(true)
      val openapi =
        OpenApiConverter.create().config(config).convertToNode(model)
      val jsonString = Node.prettyPrintJson(openapi)
      OpenApiConversionResult(protocol, serviceId, jsonString)
    }.toList
  }

  def convertWithConfig(
      model: Model,
      allowedNS: Option[Set[String]],
      buildConfig: Unit => OpenApiConfig,
      classLoader: ClassLoader
  ): List[OpenApiConversionResult] = {
    val serviceShapes =
      model.getServiceShapes().asScala.toSet[Shape]

    val filteredServices = allowedNS match {
      case None => serviceShapes
      case Some(namespaces) =>
        serviceShapes.filter(s => namespaces.contains(s.getId.getNamespace()))
    }

    filteredServices.flatMap { service =>
      convertWithConfig(
        model = model,
        service = service,
        buildConfig = buildConfig,
        classLoader = classLoader
      )
    }.toList
  }

  def convertWithConfig(
      model: Model,
      allowedNS: Option[Set[String]],
      buildConfig: Unit => OpenApiConfig
  ): List[OpenApiConversionResult] =
    convertWithConfig(
      model,
      allowedNS,
      buildConfig,
      this.getClass().getClassLoader()
    )

  /** Creates open-api representations for all services in a model that are
    * annotated with the `alloy#simpleRestJson` trait.
    */
  def convert(
      model: Model,
      allowedNS: Option[Set[String]],
      classLoader: ClassLoader
  ): List[OpenApiConversionResult] = {
    val configBuilder: Unit => OpenApiConfig = { _ =>
      new OpenApiConfig()
    }
    convertWithConfig(model, allowedNS, configBuilder, classLoader)
  }

  def convert(
      model: Model,
      allowedNS: Option[Set[String]]
  ): List[OpenApiConversionResult] =
    convert(model, allowedNS, this.getClass().getClassLoader())

  implicit class OptionalExt[A](opt: java.util.Optional[A]) {
    def asScala: Option[A] = if (opt.isPresent()) Some(opt.get()) else None
  }

}
