package alloy.openapi

import software.amazon.smithy.openapi.fromsmithy.OpenApiMapper
import software.amazon.smithy.openapi.fromsmithy.Context
import software.amazon.smithy.openapi.model.OpenApi
import scala.jdk.CollectionConverters._
import scala.jdk.OptionConverters._
import software.amazon.smithy.model.traits.Trait
import software.amazon.smithy.openapi.fromsmithy.protocols.ExtensionKeys
import software.amazon.smithy.model.node.Node
import software.amazon.smithy.openapi.model.PathItem
import software.amazon.smithy.openapi.model.OperationObject
import software.amazon.smithy.openapi.model.Ref
import software.amazon.smithy.openapi.model.ParameterObject
import software.amazon.smithy.openapi.model.ResponseObject

/** Will make all headers optional for responses which have the
  * `ExtensionKeys.shouldMakeHeadersOptional` set to true.
  */
final class MakeHeadersOptionalMapper extends OpenApiMapper {
  override def after(
      context: Context[_ <: Trait],
      openapi: OpenApi
  ): OpenApi = {
    val openBuilder = openapi.toBuilder()
    openapi.getPaths().asScala.foreach { case (pathKey, pathItem) =>
      val pathBuilder = pathItem.toBuilder()
      pathItem.getOperations().asScala.foreach { case (opMethod, op) =>
        val opBuilder = op.toBuilder()
        op.getResponses().asScala.foreach { case (responseKey, response) =>
          val responseBuilder = response.toBuilder()
          val shouldMakeHeadersOptional = response
            .getExtension(ExtensionKeys.shouldMakeHeadersOptional)
            .toScala
            .contains(Node.from(true))
          if (shouldMakeHeadersOptional) {
            response.getHeaders().asScala.foreach {
              case (headerKey, headerRef) =>
                makeHeadersOptional(
                  openapi,
                  pathBuilder,
                  opMethod,
                  opBuilder,
                  responseKey,
                  responseBuilder,
                  headerKey,
                  headerRef
                )
            }
          }
        }
      }
      openBuilder.putPath(pathKey, pathBuilder.build())

    }
    openBuilder.build()
  }

  private def makeHeadersOptional(
      openapi: OpenApi,
      pathBuilder: PathItem.Builder,
      opMethod: String,
      opBuilder: OperationObject.Builder,
      responseKey: String,
      responseBuilder: ResponseObject.Builder,
      headerKey: String,
      headerRef: Ref[ParameterObject]
  ): PathItem.Builder = {
    val deref = headerRef.deref(openapi.getComponents())
    val updated = deref.toBuilder().required(false).build()
    opBuilder.putResponse(
      responseKey,
      responseBuilder
        .putHeader(headerKey, Ref.local[ParameterObject](updated))
        .removeExtension(ExtensionKeys.shouldMakeHeadersOptional)
        .build()
    )
    updatePathItem(pathBuilder, opMethod, opBuilder.build())
  }

  private def updatePathItem(
      pi: PathItem.Builder,
      method: String,
      op: OperationObject
  ): PathItem.Builder = method.toUpperCase match {
    case "GET"     => pi.get(op)
    case "PUT"     => pi.put(op)
    case "POST"    => pi.post(op)
    case "DELETE"  => pi.delete(op)
    case "OPTIONS" => pi.options(op)
    case "HEAD"    => pi.head(op)
    case "TRACE"   => pi.trace(op)
  }
}
