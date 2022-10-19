package alloy.proto;

import software.amazon.smithy.model.node.*;
import software.amazon.smithy.utils.SmithyBuilder;

public final class ProtoReservedFieldsTraitValue {

	public static final String NUMBER = "number";
	public static final String NAME = "name";
	public static final String RANGE = "range";

	public final Integer number;
	public final String name;
	public final Range range;

	public ProtoReservedFieldsTraitValue(Builder builder) {
		this.number = builder.number;
		this.name = builder.name;
		this.range = builder.range;
	}

	public boolean isNumber() {
		return number != null;
	}

	public boolean isName() {
		return name != null;
	}

	public boolean isRange() {
		return range != null;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static ProtoReservedFieldsTraitValue fromNode(Node node) {
		ObjectNode value = node.expectObjectNode();
		Integer number = value.getNumberMember(NUMBER).map(NumberNode::getValue).map(java.lang.Number::intValue)
				.orElse(null);
		String name = value.getStringMember(NAME).map(StringNode::getValue).orElse(null);
		Range range = value.getMember(RANGE).map(Range::fromNode).orElse(null);

		return new Builder().number(number).name(name).range(range).build();
	}

	public static class Range {

		public static final String START = "start";
		public static final String END = "end";

		public final int start;
		public final int end;

		public Range(int start, int end) {
			this.start = start;
			this.end = end;
		}

		public static Range fromNode(Node node) {
			ObjectNode value = node.expectObjectNode();
			int start = value.expectNumberMember(START).getValue().intValue();
			int end = value.expectNumberMember(END).getValue().intValue();
			return new Range(start, end);
		}
	}

	public static final class Builder implements SmithyBuilder<ProtoReservedFieldsTraitValue> {
		public Integer number;
		public String name;
		public Range range;

		@Override
		public ProtoReservedFieldsTraitValue build() {
			return new ProtoReservedFieldsTraitValue(this);
		}

		public Builder number(Integer number) {
			if (number != null) {
				this.clearAll();
				this.number = number;
			}
			return this;
		}

		public Builder name(String name) {
			if (name != null) {
				this.clearAll();
				this.name = name;
			}
			return this;
		}

		public Builder range(Range range) {
			if (range != null) {
				this.clearAll();
				this.range = range;
			}
			return this;
		}

		private void clearAll() {
			this.number = null;
			this.range = null;
			this.name = null;
		}
	}

}
