package alloy.proto;

import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NumberNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.model.traits.AbstractTrait;

public final class ProtoIndexTrait extends AbstractTrait {
	public static final ShapeId ID = ShapeId.from("alloy.proto#protoIndex");

	private final int number;

	public ProtoIndexTrait(int number, FromSourceLocation sourceLocation) {
		super(ID, sourceLocation);
		this.number = number;
	}

	public ProtoIndexTrait(int number) {
		this(number, SourceLocation.NONE);
	}

	public static final class Provider extends AbstractTrait.Provider {
		public Provider() {
			super(ID);
		}

		@Override
		public Trait createTrait(ShapeId target, Node value) {
			return new ProtoIndexTrait(value.expectNumberNode().getValue().intValue(), value.getSourceLocation());
		}
	}

	public int getNumber() {
		return number;
	}

	@Override
	protected Node createNode() {
		return new NumberNode(number, getSourceLocation());
	}
}
