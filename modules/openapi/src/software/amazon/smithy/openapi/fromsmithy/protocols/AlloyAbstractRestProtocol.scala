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

package software.amazon.smithy.openapi.fromsmithy.protocols

import cats.syntax.all._
import alloy.UncheckedExamplesTrait
import software.amazon.smithy.jsonschema.Schema
import software.amazon.smithy.model.knowledge.HttpBinding.Location
import software.amazon.smithy.model.knowledge._
import software.amazon.smithy.model.node.Node
import software.amazon.smithy.model.shapes._
import software.amazon.smithy.model.traits._
import software.amazon.smithy.openapi.OpenApiException
import software.amazon.smithy.openapi.fromsmithy.Context
import software.amazon.smithy.openapi.fromsmithy.OpenApiProtocol
import software.amazon.smithy.openapi.fromsmithy.OpenApiProtocol.Operation
import software.amazon.smithy.openapi.model._

import java.util
import java.util.function.Function
import scala.jdk.CollectionConverters._
import software.amazon.smithy.model.traits.ExamplesTrait.Example

/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

/** Provides the shared functionality used across protocols that use Smithy's
  * HTTP binding traits.
  *
  * <p>This class handles adding query string, path, header, payload, and
  * document bodies to HTTP messages using an HttpBindingIndex. Inline schemas
  * as created for query string, headers, and path parameters that do not
  * utilize the correct types or set an explicit type/format (for example, this
  * class ensures that a timestamp shape serialized in the query string is
  * serialized using the date-time format).
  *
  * <p>This class is currently package-private, but may be made public in the
  * future when we're sure about its API.
  */
object AlloyAbstractRestProtocol {

  sealed trait MessageType
  object MessageType {
    case object REQUEST extends MessageType
    case object RESPONSE extends MessageType
    case object ERROR extends MessageType
  }

}

abstract class AlloyAbstractRestProtocol[T <: Trait]
    extends OpenApiProtocol[T] {

  /** Gets the media type of a document sent in a request or response.
    */
  def getDocumentMediaType(): String

  /** Creates a schema to send a document payload in the request, response, or
    * error of an operation.
    *
    * @param context
    *   Conversion context.
    * @param operationOrError
    *   Operation shape or error shape.
    * @param bindings
    *   HTTP bindings of this shape.
    * @param messageType
    *   The message type (request, response, or error).
    * @return
    *   Returns the created document schema.
    */
  def createDocumentSchema(
      context: Context[T],
      operationOrError: Shape,
      bindings: List[HttpBinding],
      messageType: AlloyAbstractRestProtocol.MessageType
  ): Schema

  override def createOperation(context: Context[T], operation: OperationShape) =
    operation
      .getTrait(classOf[HttpTrait])
      .asScala
      .map((_: HttpTrait) => {
        val method =
          context.getOpenApiProtocol.getOperationMethod(context, operation)
        val uri =
          context.getOpenApiProtocol.getOperationUri(context, operation)
        val builder =
          OperationObject.builder.operationId(operation.getId.getName)
        val bindingIndex = HttpBindingIndex.of(context.getModel)
        createPathParameters(context, operation).foreach(builder.addParameter)
        createQueryParameters(context, operation).foreach(builder.addParameter)
        createRequestHeaderParameters(context, operation)
          .foreach(builder.addParameter)
        createRequestBody(context, bindingIndex, operation)
          .foreach(builder.requestBody)
        createResponses(context, bindingIndex, operation)
          .foreach { case (k, values) =>
            combineResponseContent(values).foreach(v =>
              builder.putResponse(k, v)
            )
          }
        Operation.create(method, uri, builder)
      })
      .asJava

  def combineResponseContent(
      responses: List[ResponseObject]
  ): Option[ResponseObject] = {
    responses match {
      case Nil         => None
      case head :: Nil => Some(head)
      case head :: tail =>
        val all = head +: tail
        val mediaTypeObjects: List[MediaTypeObject] =
          all.flatMap(_.getContent().asScala.toList.map {
            case (_, mediaTypeObject) => mediaTypeObject
          })
        val schemas =
          mediaTypeObjects.flatMap(_.getSchema().asScala)
        val newSchema = Schema.builder().oneOf(schemas.asJava).build()
        val newExamples = mediaTypeObjects
          .flatMap(_.getExamples().asScala.toList)
          .toMap
          .map { case (k, exampleObj) => k -> exampleObj.toNode() }
        val media = MediaTypeObject.builder
          .examples(
            newExamples.asJava
          )
          .schema(newSchema)
          .build()
        // `AlloyAbstractRestProtocol` only supports a single content-type, application/json
        // This is why we can just use the content from `head` here
        val newContent =
          head.getContent().asScala.map { case (k, _) => k -> media }
        Some(head.toBuilder().content(newContent.asJava).build())
    }
  }

  def createPathParameters(
      context: Context[T],
      operation: OperationShape
  ) = {
    val bindingIndex = HttpBindingIndex.of(context.getModel)
    val httpTrait = operation.expectTrait(classOf[HttpTrait])

    for (
      binding <- bindingIndex
        .getRequestBindings(
          operation,
          HttpBinding.Location.LABEL
        )
        .asScala
    ) yield {
      val schema = createPathParameterSchema(context, binding)
      val memberName = binding.getMemberName
      val label = httpTrait.getUri
        .getLabel(memberName)
        .orElseThrow(() =>
          new OpenApiException(
            String.format(
              "Unable to find URI label on %s for %s: %s",
              operation.getId,
              binding.getMemberName,
              httpTrait.getUri
            )
          )
        )
      // Greedy labels in OpenAPI need to include the label in the generated parameter.
      // For example, given "/{foo+}", the parameter name must be "foo+".
      // Some vendors/tooling, require the "+" suffix be excluded in the generated parameter.
      // If required, the setRemoveGreedyParameterSuffix config option should be set to `true`.
      // When this option is enabled, given "/{foo+}", the parameter name will be "foo".
      var name = label.getContent
      if (
        label.isGreedyLabel && !context.getConfig.getRemoveGreedyParameterSuffix
      ) name = name + "+"

      val builder = ModelUtils
        .createParameterMember(context, binding.getMember)
        .name(name)
        .in("path")
        .schema(schema)

      createInputExamples(operation, memberName).foreach(builder.examples)

      builder.build
    }
  }

  private def createPathParameterSchema(
      context: Context[T],
      binding: HttpBinding
  ) = {
    val member = binding.getMember

    if (context.getJsonSchemaConverter.isInlined(member))
      context.getJsonSchemaConverter.convertShape(member).getRootSchema
    else context.createRef(binding.getMember)
  }

  private def getHeaderTimestampFormat(
      context: Context[_ <: Trait],
      member: MemberShape
  ): Option[String] = {
    if (
      context.getModel
        .getShape(member.getTarget)
        .filter(s => s.isTimestampShape)
        .isPresent()
    ) {
      Some(
        member
          .getMemberTrait(context.getModel, classOf[TimestampFormatTrait])
          .asScala
          .map(_.getValue())
          .getOrElse("http-date")
      )
    } else {
      None
    }
  }

  // Creates parameters that appear in the query string. Each input member
  // bound to the QUERY location will generate a new ParameterObject that
  // has a location of "query".
  private def createQueryParameters(
      context: Context[T],
      operation: OperationShape
  ) = {
    val httpBindingIndex = HttpBindingIndex.of(context.getModel)
    for (
      binding <- httpBindingIndex
        .getRequestBindings(
          operation,
          HttpBinding.Location.QUERY
        )
        .asScala
    ) yield {
      val member = binding.getMember
      val param = ModelUtils
        .createParameterMember(context, member)
        .in("query")
        .name(binding.getLocationName)
      val target = context.getModel.expectShape(member.getTarget)
      // List and set shapes in the query string are repeated, so we need to "explode" them
      // using the "form" style (e.g., "foo=bar&foo=baz").
      // See https://swagger.io/specification/#style-examples
      if (target.isInstanceOf[CollectionShape])
        param.style("form").explode(true)
      // Create the appropriate schema based on the shape type.
      val refSchema = context.inlineOrReferenceSchema(member)
      val visitor = new QuerySchemaVisitor[T](context, refSchema, member)
      param.schema(target.accept(visitor))
      createInputExamples(operation, binding.getMemberName).foreach(
        param.examples
      )

      param.build
    }
  }

  private def createRequestHeaderParameters(
      context: Context[T],
      operation: OperationShape
  ) = {
    val bindings = HttpBindingIndex
      .of(context.getModel)
      .getRequestBindings(operation, HttpBinding.Location.HEADER)
    createHeaderParameters(
      context,
      bindings,
      operation,
      AbstractRestProtocol.MessageType.REQUEST
    ).values
  }

  private def createHeaderParameters(
      context: Context[T],
      bindings: util.List[HttpBinding],
      operation: Shape,
      messageType: AbstractRestProtocol.MessageType
  ) = {
    val result = for (binding <- bindings.asScala) yield {
      val member = binding.getMember
      val param = ModelUtils.createParameterMember(context, member)

      if (messageType eq AbstractRestProtocol.MessageType.REQUEST) {
        param.in("header").name(binding.getLocationName)
        createInputExamples(operation, binding.getMemberName)
          .foreach(param.examples)
      } else { // Response headers don't use "in" or "name".
        param.in(null).name(null)
        createOutputExamples(operation, binding.getMemberName)
          .foreach(param.examples)
      }
      val target = context.getModel.expectShape(member.getTarget)
      val startingSchema = context.inlineOrReferenceSchema(member)
      val visitor = new HeaderSchemaVisitor[T](context, startingSchema, member)
      val visitedSchema = target.accept(visitor)
      val schemaVerified = getHeaderTimestampFormat(context, member) match {
        case None => visitedSchema
        case Some(format) =>
          val copiedBuilder = ModelUtils.convertSchemaToStringBuilder(
            visitedSchema
          )
          copiedBuilder.format(format).build
      }
      param.schema(schemaVerified)
      binding.getLocationName -> param.build
    }
    result.toMap
  }

  private def createRequestBody(
      context: Context[T],
      bindingIndex: HttpBindingIndex,
      operation: OperationShape
  ) = {
    val payloadBindings =
      bindingIndex.getRequestBindings(operation, HttpBinding.Location.PAYLOAD)
    // Get the default media type if one cannot be resolved.
    val mediaType =
      determineContentType(
        bindingIndex.getRequestBindings(operation).values().asScala
      )
    if (payloadBindings.isEmpty)
      createRequestDocument(context, bindingIndex, operation)
    else
      createRequestPayload(
        mediaType,
        context,
        payloadBindings.get(0),
        operation
      )
  }

  private def createRequestPayload(
      mediaTypeRange: Option[String],
      context: Context[T],
      binding: HttpBinding,
      operation: OperationShape
  ) = { // API Gateway validation requires that in-line schemas must be objects
    // or arrays. These schemas are synthesized as references so that
    // any schemas with string types will pass validation.
    val schema = context.inlineOrReferenceSchema(binding.getMember)
    val mediaTypeObject = getMediaTypeObject(
      context,
      schema,
      operation,
      (shape: Shape) => {
        val shapeName = shape.getId.getName
        shapeName + "InputPayload"
      }
    )
    val mtr = mediaTypeRange.getOrElse(getDocumentMediaType())

    val updatedMtObject = createInputExamples(operation, binding.getMemberName)
      .map(mediaTypeObject.toBuilder.examples(_).build)
      .getOrElse(mediaTypeObject)

    val requestBodyObject = RequestBodyObject.builder
      .putContent(mtr, updatedMtObject)
      .required(binding.getMember.isRequired)
      .build
    Some(requestBodyObject)
  }

  private def createRequestDocument(
      context: Context[T],
      bindingIndex: HttpBindingIndex,
      operation: OperationShape
  ): Option[RequestBodyObject] = {
    val bindings =
      bindingIndex.getRequestBindings(operation, HttpBinding.Location.DOCUMENT)
    // If nothing is bound to the document, then no schema needs to be synthesized.
    if (bindings.isEmpty) None
    else {
      // Synthesize a schema for the body of the request.
      val schema = createDocumentSchema(
        context,
        operation,
        bindings.asScala.toList,
        AlloyAbstractRestProtocol.MessageType.REQUEST
      )
      val synthesizedName = operation.getId.getName + "RequestContent"
      val pointer = context.putSynthesizedSchema(synthesizedName, schema)

      val memberNames = bindings.asScala.toList.map(_.getMemberName)
      val maybeExamples =
        createExamples(operation)(ExampleNode.forInputMembers(_, memberNames))
      val builder =
        MediaTypeObject.builder.schema(Schema.builder.ref(pointer).build)
      maybeExamples.foreach(builder.examples)
      val mediaTypeObject = builder.build
      // If any of the top level bindings are required, then the body itself must be required.
      val required = bindings.asScala.exists(_.getMember.isRequired)
      Some(
        RequestBodyObject.builder
          .putContent(getDocumentMediaType(), mediaTypeObject)
          .required(required)
          .build
      )
    }
  }

  private def createResponses(
      context: Context[T],
      bindingIndex: HttpBindingIndex,
      operation: OperationShape
  ) = {
    // Hack to ensure that the model contains the potentially updated
    // operation shape.
    val updatedModel =
      context.getModel().toBuilder().addShape(operation).build()
    val result = new util.TreeMap[String, List[ResponseObject]]
    val operationIndex = OperationIndex.of(updatedModel)
    operationIndex
      .getOutputShape(operation)
      .asScala
      .foreach((output: StructureShape) => {
        updateResponsesMapWithResponseStatusAndObject(
          context,
          bindingIndex,
          operation,
          output,
          result
        )
      })
    for (error <- operationIndex.getErrors(operation).asScala) {
      updateResponsesMapWithResponseStatusAndObject(
        context,
        bindingIndex,
        operation,
        error,
        result
      )
    }
    result.asScala
  }

  private def reorganizeExampleTraits(
      operation: OperationShape,
      shape: StructureShape
  ): Shape = {
    val isErrorShape = shape.hasTrait(classOf[ErrorTrait])
    val operationOrError =
      if (isErrorShape) shape
      else operation

    val allExamples: List[Example] =
      operation
        .getTrait(classOf[ExamplesTrait])
        .asScala
        .toList
        .flatMap(_.getExamples.asScala)
    val allRelevantExamples: List[Example] =
      // error response so only include examples that are matching to this error shape
      if (isErrorShape)
        allExamples
          .filter(
            _.getError().asScala.map(_.getShapeId).contains(shape.toShapeId)
          )
      // not an error response so no error examples should be included
      else allExamples.filter(_.getError().isEmpty())
    val newExamplesTraitBuilder = ExamplesTrait.builder()
    allRelevantExamples.foreach { ex => newExamplesTraitBuilder.addExample(ex) }
    val exTrait: Trait = newExamplesTraitBuilder.build()
    val newShape: Shape =
      (Shape.shapeToBuilder(operationOrError): AbstractShapeBuilder[_, _])
        .addTrait(exTrait)
        .build()

    newShape
  }

  private def updateResponsesMapWithResponseStatusAndObject(
      context: Context[T],
      bindingIndex: HttpBindingIndex,
      operation: OperationShape,
      shape: StructureShape,
      responses: util.Map[String, List[ResponseObject]]
  ) = {
    val operationOrError = reorganizeExampleTraits(operation, shape)
    val statusCode = context.getOpenApiProtocol.getOperationResponseStatusCode(
      context,
      operationOrError
    )
    val response = createResponse(
      context,
      bindingIndex,
      statusCode,
      operationOrError
    )
    val currentResponses: Option[List[ResponseObject]] = Option(
      responses.get(statusCode)
    )
    val updatedResponses = currentResponses match {
      case Some(current) => current :+ response
      case None          => List(response)
    }
    responses.put(statusCode, updatedResponses)
  }

  private def createResponse(
      context: Context[T],
      bindingIndex: HttpBindingIndex,
      statusCode: String,
      operationOrError: Shape
  ) = {
    val responseBuilder = ResponseObject.builder
    responseBuilder.description(
      String.format(
        "%s %s response",
        operationOrError.getId.getName,
        statusCode
      )
    )
    createResponseHeaderParameters(context, operationOrError).foreach {
      case (k: String, v: ParameterObject) =>
        responseBuilder.putHeader(k, Ref.local(v))
    }
    addResponseContent(
      context,
      bindingIndex,
      responseBuilder,
      operationOrError
    )
    responseBuilder.build
  }

  private def createResponseHeaderParameters(
      context: Context[T],
      operationOrError: Shape
  ) = {
    val bindings = HttpBindingIndex
      .of(context.getModel)
      .getResponseBindings(operationOrError, HttpBinding.Location.HEADER)
    createHeaderParameters(
      context,
      bindings,
      operationOrError,
      AbstractRestProtocol.MessageType.RESPONSE
    )
  }

  private def addResponseContent(
      context: Context[T],
      bindingIndex: HttpBindingIndex,
      responseBuilder: ResponseObject.Builder,
      operationOrError: Shape
  ) = {
    val payloadBindings = bindingIndex.getResponseBindings(
      operationOrError,
      HttpBinding.Location.PAYLOAD
    )
    val mediaType = determineContentType(
      bindingIndex.getResponseBindings(operationOrError).values().asScala
    )
    if (!payloadBindings.isEmpty)
      createResponsePayload(
        mediaType,
        context,
        payloadBindings.get(0),
        responseBuilder,
        operationOrError
      )
    else
      createResponseDocumentIfNeeded(
        getDocumentMediaType(),
        context,
        bindingIndex,
        responseBuilder,
        operationOrError
      )
  }

  private def createResponsePayload(
      mediaType: Option[String],
      context: Context[T],
      binding: HttpBinding,
      responseBuilder: ResponseObject.Builder,
      operationOrError: Shape
  ) = {
    val schema = context.inlineOrReferenceSchema(binding.getMember)
    val mediaTypeObject = getMediaTypeObject(
      context,
      schema,
      operationOrError,
      (shape: Shape) => {
        val shapeName = shape.getId.getName
        if (shape.isInstanceOf[OperationShape]) shapeName + "OutputPayload"
        else shapeName + "ErrorPayload"
      }
    )

    val updatedMtObject =
      createOutputExamples(operationOrError, binding.getMemberName)
        .map(mediaTypeObject.toBuilder.examples(_).build)
        .getOrElse(mediaTypeObject)

    mediaType.foreach { mt =>
      responseBuilder.putContent(mt, updatedMtObject)
    }
  }

  // If a synthetic schema is just a wrapper for another schema, create the
  // MediaTypeObject using the pointer to the existing schema, otherwise add
  // the synthetic schema and create the MediaTypeObject using a new pointer.
  private def getMediaTypeObject(
      context: Context[T],
      schema: Schema,
      shape: Shape,
      createSynthesizedName: Function[Shape, String]
  ) = if (!schema.getType.isPresent && schema.getRef.isPresent)
    MediaTypeObject.builder
      .schema(Schema.builder.ref(schema.getRef.get).build)
      .build
  else {
    val synthesizedName = createSynthesizedName.apply(shape)
    val pointer = context.putSynthesizedSchema(synthesizedName, schema)
    MediaTypeObject.builder.schema(Schema.builder.ref(pointer).build).build
  }

  private def createResponseDocumentIfNeeded(
      mediaType: String,
      context: Context[T],
      bindingIndex: HttpBindingIndex,
      responseBuilder: ResponseObject.Builder,
      operationOrError: Shape
  ): ResponseObject.Builder = {
    val bindings = bindingIndex.getResponseBindings(
      operationOrError,
      HttpBinding.Location.DOCUMENT
    )
    // If the operation doesn't have any document bindings, then do nothing.
    if (bindings.isEmpty) responseBuilder
    else {
      // Document bindings needs to be synthesized into a new schema that contains
      // just the document bindings separate from other parameters.
      val messageType =
        if (operationOrError.isInstanceOf[OperationShape])
          AlloyAbstractRestProtocol.MessageType.RESPONSE
        else AlloyAbstractRestProtocol.MessageType.ERROR
      // This "synthesizes" a new schema that just contains the document bindings.
      // While we *could* just use the referenced output/error shape as-is, that
      // would be a bad idea; traits applied to shapes in Smithy can contextually
      // influence what the resulting JSON schema or OpenAPI. Consider the
      // following examples:
      //
      // 1. If the same shape is reused as input and output, then some members
      //    might be bound to query string parameters, and query string params
      //    aren't relevant on output. Trying to use the same schema derived
      //    from the reused input/output shape would result in a broken API.
      // 2. What if the input/output shape doesn't bind anything to the query
      //    string, headers, path, etc? Couldn't it then be used as-is with
      //    the name given in the Smithy model? Yes, technically it could, but
      //    that's also a bad idea. If/when you want to add a header or query
      //    string parameter, then you now need to break your generated OpenAPI
      //    schema, particularly if the shapes was reused throughout your model
      //    outside of top-level inputs, outputs, and errors.
      // The safest thing to do here is to always synthesize a new schema that
      // just includes the document bindings.
      // **NOTE: this same blurb applies to why we do this on input.**
      val schema =
        createDocumentSchema(
          context,
          operationOrError,
          bindings.asScala.toList,
          messageType
        )
      val synthesizedName = operationOrError.getId.getName + "ResponseContent"
      val pointer = context.putSynthesizedSchema(synthesizedName, schema)

      val memberNames = bindings.asScala.toList.map(_.getMemberName)
      val maybeExamples = createExamples(operationOrError)(
        ExampleNode.forOutputMembers(_, memberNames)
      )
      val builder =
        MediaTypeObject.builder.schema(Schema.builder.ref(pointer).build)
      maybeExamples.foreach(builder.examples)
      responseBuilder.putContent(mediaType, builder.build)
    }
  }

  def determineContentType(bindings: Iterable[HttpBinding]) = {
    val locations = Set(Location.DOCUMENT, Location.PAYLOAD)
    bindings.collectFirst {
      case binding if locations(binding.getLocation()) =>
        getDocumentMediaType()
    }
  }

  private def createInputExamples(operation: Shape, memberName: String) =
    createExamples(operation)(ExampleNode.forInputMember(_, memberName))

  private def createOutputExamples(operation: Shape, memberName: String) =
    createExamples(operation)(ExampleNode.forOutputMember(_, memberName))

  private def createExamples(
      operation: Shape
  )(
      createNode: ExamplesTrait.Example => ExampleNode
  ): Option[util.Map[String, Node]] = {
    val maybeCheckedExamples: Option[List[ExamplesTrait.Example]] =
      operation.getTrait(classOf[ExamplesTrait]).asScala.map { exampleTrait =>
        exampleTrait.getExamples.asScala.toList
      }

    val maybeUncheckedExamples: Option[List[ExamplesTrait.Example]] =
      operation.getTrait(classOf[UncheckedExamplesTrait]).asScala.map {
        uncheckedExampleTrait =>
          uncheckedExampleTrait
            .getExamples()
            .asScala
            .map { unchecked =>
              val builder =
                ExamplesTrait.Example
                  .builder()
                  .title(unchecked.getTitle())
                  .input(unchecked.getInput())
                  .output(unchecked.getOutput())
              if (unchecked.getDocumentation().isPresent()) {
                builder.documentation(unchecked.getDocumentation().get())
              }
              builder.build()
            }
            .toList
      }

    (maybeCheckedExamples |+| maybeUncheckedExamples).map { examples =>
      examples
        .map(createNode(_).build)
        .collect { case Some(exampleNode) => exampleNode }
        .toMap
        .asJava
    }
  }
}
