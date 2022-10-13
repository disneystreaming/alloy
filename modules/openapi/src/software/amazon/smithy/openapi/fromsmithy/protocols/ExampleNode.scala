package software.amazon.smithy.openapi.fromsmithy.protocols

import software.amazon.smithy.model.SourceLocation
import software.amazon.smithy.model.node._
import software.amazon.smithy.model.traits.ExamplesTrait.Example

private[protocols] final case class ExampleNode(
    title: String,
    summary: Option[String],
    value: Option[Node]
) {

  def build: Option[(String, Node)] =
    value.map { example =>
      val summaryNode =
        summary.map(s => new StringNode(s, SourceLocation.NONE))
      title -> Node.objectNodeBuilder
        .withOptionalMember("summary", summaryNode.asJava)
        .withMember("value", example)
        .build
        .toNode
    }
}

object ExampleNode {

  private[protocols] def forInputMember(
      example: Example,
      memberName: String
  ): ExampleNode =
    ExampleNode(example, example.getInput.getMember(memberName).asScala)

  private[protocols] def forInputMembers(
      example: Example,
      memberNames: List[String]
  ): ExampleNode = {
    val members: List[(String, Node)] = memberNames.flatMap(name =>
      example.getInput.getMember(name).asScala.map(name -> _)
    )
    ExampleNode(example, members)
  }

  private[protocols] def forOutputMember(
      example: Example,
      memberName: String
  ): ExampleNode =
    ExampleNode(example, example.getOutput.getMember(memberName).asScala)

  private[protocols] def forOutputMembers(
      example: Example,
      memberNames: List[String]
  ): ExampleNode = {
    val members: List[(String, Node)] = memberNames.flatMap(name =>
      example.getOutput.getMember(name).asScala.map(name -> _)
    )
    ExampleNode(example, members)
  }

  private def apply(
      example: Example,
      members: List[(String, Node)]
  ): ExampleNode = {
    val builder = Node.objectNodeBuilder
    members.foreach { case (name, member) =>
      builder.withMember(name, member)
    }
    ExampleNode(example, Some(builder.build))
  }

  private def apply(example: Example, value: Option[Node]): ExampleNode =
    ExampleNode(
      title = example.getTitle,
      summary = example.getDocumentation.asScala,
      value = value
    )
}
