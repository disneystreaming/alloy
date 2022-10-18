package alloy.proto;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;
import software.amazon.smithy.model.traits.AbstractTrait;

public class ProtoEnabledTrait extends AnnotationTrait {

	public static ShapeId ID = ShapeId.from("alloy.proto#protoEnabled");

	public ProtoEnabledTrait(ObjectNode node) {
		super(ID, node);
	}

	public ProtoEnabledTrait() {
		super(ID, Node.objectNode());
	}

	public static final class Provider extends AbstractTrait.Provider {
		public Provider() {
			super(ID);
		}

		@Override
		public ProtoEnabledTrait createTrait(ShapeId target, Node node) {
			return new ProtoEnabledTrait(node.expectObjectNode());
		}
	}
}
