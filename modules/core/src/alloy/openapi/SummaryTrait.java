package alloy.openapi;

import software.amazon.smithy.model.SourceException;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.StringTrait;

public class SummaryTrait extends StringTrait {

    public static ShapeId ID = ShapeId.from("alloy.openapi#summary");

    public String getSummary() {
        return getValue();
    }

    public SummaryTrait(String value, SourceLocation sourceLocation) {
        super(ID, value, sourceLocation);

        if (getSummary().isEmpty()) {
            throw new SourceException("summary must not be empty", getSourceLocation());
        }
    }

    public SummaryTrait(String value) {
        this(value, SourceLocation.NONE);
    }

    public static final class Provider extends StringTrait.Provider<SummaryTrait> {
        public Provider() {
            super(ID, SummaryTrait::new);
        }
    }
}

