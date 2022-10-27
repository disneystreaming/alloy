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
