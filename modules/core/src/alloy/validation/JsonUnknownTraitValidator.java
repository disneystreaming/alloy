package alloy.validation;

import alloy.JsonUnknownTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.shapes.UnionShape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class JsonUnknownTraitValidator extends AbstractValidator {
    public final static ShapeId ID = ShapeId.from("alloy#jsonUnknown");

    @Override
    public List<ValidationEvent> validate(Model model) {
        return model.getMemberShapesWithTrait(JsonUnknownTrait.class)
                .stream().map(member -> model.expectShape(member.getId().withoutMember()))
                .filter(Shape::isUnionShape)
                .distinct()
                .map(s -> s.asUnionShape().orElseThrow(() -> new IllegalStateException("Expected union shape.")))
                .flatMap(this::validateUnionShape).collect(Collectors.toList());
    }

    private Stream<? extends ValidationEvent> validateUnionShape(UnionShape unionShape) {
        return validateTraitExclusivity(unionShape);
    }

    /*
     * Ensures the container only has one member with the trait
     * */
    private Stream<ValidationEvent> validateTraitExclusivity(UnionShape unionShape) {
        List<MemberShape> membersWithTrait = unionShape.members().stream().filter(member -> member.hasTrait(ID)).collect(Collectors.toList());

        if (membersWithTrait.size() > 1) return Stream.of(
                error(membersWithTrait.get(0), String.format("%s trait was used multiple times within the same union, this is not allowed.", ID), "ConflictingUnionMember")
        );
        else return Stream.empty();
    }
}
