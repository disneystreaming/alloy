package alloy.proto;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;
import software.amazon.smithy.model.traits.AbstractTrait;

public class GrpcTrait extends AnnotationTrait {

	public static ShapeId ID = ShapeId.from("alloy.proto#grpc");

	public GrpcTrait(ObjectNode node) {
		super(ID, node);
	}

	public GrpcTrait() {
		super(ID, Node.objectNode());
	}

	public static final class Provider extends AbstractTrait.Provider {
		public Provider() {
			super(ID);
		}

		@Override
		public GrpcTrait createTrait(ShapeId target, Node node) {
			return new GrpcTrait(node.expectObjectNode());
		}
	}
}
