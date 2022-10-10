package alloy;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;
import software.amazon.smithy.model.traits.AbstractTrait;

public class RestJsonTrait extends AnnotationTrait {

	public static ShapeId ID = ShapeId.from("alloy#restJson");

	public RestJsonTrait(ObjectNode node) {
		super(ID, node);
	}

	public RestJsonTrait() {
		super(ID, Node.objectNode());
	}

	public static final class Provider extends AbstractTrait.Provider {
		public Provider() {
			super(ID);
		}

		@Override
		public RestJsonTrait createTrait(ShapeId target, Node node) {
			return new RestJsonTrait(node.expectObjectNode());
		}
	}
}
