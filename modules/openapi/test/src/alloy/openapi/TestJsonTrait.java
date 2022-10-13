package alloy.openapi;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;
import software.amazon.smithy.model.traits.AbstractTrait;

public class TestJsonTrait extends AnnotationTrait {

	public static ShapeId ID = ShapeId.from("bar#testJson");

	public TestJsonTrait(ObjectNode node) {
		super(ID, node);
	}

	public TestJsonTrait() {
		super(ID, Node.objectNode());
	}

	public static final class Provider extends AbstractTrait.Provider {
		public Provider() {
			super(ID);
		}

		@Override
		public TestJsonTrait createTrait(ShapeId target, Node node) {
			return new TestJsonTrait(node.expectObjectNode());
		}
	}
}
