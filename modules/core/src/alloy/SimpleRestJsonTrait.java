package alloy;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;
import software.amazon.smithy.model.traits.AbstractTrait;

public class SimpleRestJsonTrait extends AnnotationTrait {

	public static ShapeId ID = ShapeId.from("alloy#simpleRestJson");

	public SimpleRestJsonTrait(ObjectNode node) {
		super(ID, node);
	}

	public SimpleRestJsonTrait() {
		super(ID, Node.objectNode());
	}

	public static final class Provider extends AbstractTrait.Provider {
		public Provider() {
			super(ID);
		}

		@Override
		public SimpleRestJsonTrait createTrait(ShapeId target, Node node) {
			return new SimpleRestJsonTrait(node.expectObjectNode());
		}
	}
}
