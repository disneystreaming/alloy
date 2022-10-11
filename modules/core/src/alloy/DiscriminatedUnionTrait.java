package alloy;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.StringTrait;

public final class DiscriminatedUnionTrait extends StringTrait {

	public static final ShapeId ID = ShapeId.from("alloy#discriminated");

	public DiscriminatedUnionTrait(String value, SourceLocation sourceLocation) {
		super(ID, value, sourceLocation);
	}

	public DiscriminatedUnionTrait(String value) {
		this(value, SourceLocation.NONE);
	}

	public static final class Provider extends StringTrait.Provider<DiscriminatedUnionTrait> {
		public Provider() {
			super(ID, DiscriminatedUnionTrait::new);
		}
	}
}
