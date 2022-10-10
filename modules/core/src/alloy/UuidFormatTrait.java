package alloy;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AnnotationTrait;

public final class UuidFormatTrait extends AnnotationTrait {
	public static ShapeId ID = ShapeId.from("alloy#uuidFormat");

	public UuidFormatTrait() {
		super(ID, Node.objectNode());
	}

	public static final class Provider extends AnnotationTrait.Provider<UuidFormatTrait> {
		public Provider() {
			super(ID, (node) -> new UuidFormatTrait());
		}
	}
}
