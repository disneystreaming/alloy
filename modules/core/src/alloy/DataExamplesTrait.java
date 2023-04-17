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

package alloy;

import software.amazon.smithy.model.node.ArrayNode;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.model.traits.TraitService;
import software.amazon.smithy.utils.ToSmithyBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class DataExamplesTrait extends AbstractTrait implements ToSmithyBuilder<DataExamplesTrait> {
	public static final ShapeId ID = ShapeId.from("alloy#dataExamples");

	public enum DataExampleType {
		STRING, JSON, SMITHY
	}

	public static final class DataExample {
		private final DataExampleType exampleType;
		private final Node content;

		public DataExample(DataExampleType type, Node content) {
			this.exampleType = type;
			this.content = content;
		}

		public DataExampleType getExampleType() {
			return exampleType;
		}

		public Node getContent() {
			return content;
		}
	}

	private final List<DataExample> examples;

	private DataExamplesTrait(Builder builder) {
		super(ID, builder.getSourceLocation());
		this.examples = new ArrayList<>(builder.examples);
	}

	public List<DataExample> getExamples() {
		return examples;
	}

	@Override
	protected Node createNode() {
		return examples.stream()
			.map(ex -> ObjectNode.builder().withMember(ex.exampleType.name().toLowerCase(), ex.content).build())
			.collect(ArrayNode.collect(getSourceLocation()));
	}

	@Override
	public Builder toBuilder() {
		Builder builder = new Builder().sourceLocation(getSourceLocation());
		examples.forEach(builder::addExample);
		return builder;
	}

	/**
	 * @return Returns a builder used to create an examples trait.
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder extends AbstractTraitBuilder<DataExamplesTrait, Builder> {
		private final List<DataExample> examples = new ArrayList<>();

		public Builder addExample(DataExample example) {
			examples.add(Objects.requireNonNull(example));
			return this;
		}

		public Builder clearExamples() {
			examples.clear();
			return this;
		}

		@Override
		public DataExamplesTrait build() {
			return new DataExamplesTrait(this);
		}
	}

	public static final class Provider implements TraitService {
		@Override
		public ShapeId getShapeId() {
			return ID;
		}

		public DataExamplesTrait createTrait(ShapeId target, Node value) {
			Builder builder = builder().sourceLocation(value);
			value.expectArrayNode().forEach(node -> {
				Optional<ObjectNode> maybeNode = node.asObjectNode();
				if (maybeNode.isPresent()) {
					DataExampleType type;
					if (maybeNode.get().containsMember("smithy")) {
						type = DataExampleType.SMITHY;
					} else if (maybeNode.get().containsMember("json")) {
						type = DataExampleType.JSON;
					} else {
						type = DataExampleType.STRING;
					}
					Node n = maybeNode.get().expectMember(type.name().toLowerCase());
					builder.addExample(new DataExample(type, n));
				}
			});
			DataExamplesTrait result = builder.build();
			result.setNodeCache(value);
			return result;
		}
	}

}
