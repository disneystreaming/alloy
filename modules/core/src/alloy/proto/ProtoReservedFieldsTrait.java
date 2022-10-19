package alloy.proto;

import java.util.List;
import java.util.ArrayList;

import software.amazon.smithy.model.node.*;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.*;
import software.amazon.smithy.utils.ListUtils;

public final class ProtoReservedFieldsTrait extends AbstractTrait {
	public static final ShapeId ID = ShapeId.from("alloy.proto#protoReservedFields");

	private final List<alloy.proto.ProtoReservedFieldsTraitValue> reserved;

	public ProtoReservedFieldsTrait(Builder builder) {
		super(ID, builder.getSourceLocation());
		this.reserved = ListUtils.copyOf(builder.reserved);
	}

	public List<alloy.proto.ProtoReservedFieldsTraitValue> getReserved() {
		return this.reserved;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder extends AbstractTraitBuilder<ProtoReservedFieldsTrait, Builder> {
		private final List<alloy.proto.ProtoReservedFieldsTraitValue> reserved = new ArrayList<>();

		public Builder add(alloy.proto.ProtoReservedFieldsTraitValue reserved) {
			this.reserved.add(reserved);
			return this;
		}

		@Override
		public ProtoReservedFieldsTrait build() {
			return new ProtoReservedFieldsTrait(this);
		}
	}

	public static final class Provider extends AbstractTrait.Provider {
		public Provider() {
			super(ID);
		}

		@Override
		public ProtoReservedFieldsTrait createTrait(ShapeId target, Node value) {
			Builder builder = new Builder().sourceLocation(value);
			for (ObjectNode definition : value.expectArrayNode().getElementsAs(ObjectNode.class)) {
				builder.add(alloy.proto.ProtoReservedFieldsTraitValue.fromNode(definition));
			}
			return builder.build();
		}
	}

	@Override
	protected Node createNode() {
		return getReserved().stream().map(reserved -> {
			ObjectNode.Builder builder = Node.objectNodeBuilder();
			if (reserved.isNumber()) {
				builder.withMember("number", reserved.number);
			}
			if (reserved.isName()) {
				builder.withMember("name", reserved.name);
			}
			if (reserved.isRange()) {
				ObjectNode.Builder rangeBuilder = Node.objectNodeBuilder();
				rangeBuilder.withMember("start", reserved.range.start);
				rangeBuilder.withMember("end", reserved.range.end);
				builder.withMember("range", rangeBuilder.build());
			}
			return builder.build();
		}).collect(ArrayNode.collect(getSourceLocation()));
	}
}
