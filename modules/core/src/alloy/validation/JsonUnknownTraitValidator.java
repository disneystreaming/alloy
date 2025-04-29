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
