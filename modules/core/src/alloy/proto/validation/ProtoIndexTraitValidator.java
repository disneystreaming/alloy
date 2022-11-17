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

package alloy.proto.validation;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import alloy.validation.OptionHelper;
import alloy.proto.ProtoIndexTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.*;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

final public class ProtoIndexTraitValidator extends AbstractValidator {
	public final static String INCONSISTENT_PROTO_INDEXES = "InconsistentProtoIndexes";
	public final static String DUPLICATED_PROTO_INDEX = "DuplicatedProtoIndex";

	@Override
	public List<ValidationEvent> validate(Model model) {
		final Set<ShapeId> uniqueShapes = model.getMemberShapesWithTrait(ProtoIndexTrait.class).stream()
				.map(MemberShape::getContainer).collect(Collectors.toSet());
		return uniqueShapes.stream().flatMap(shapeId -> OptionHelper.toStream(model.getShape(shapeId)))
				.flatMap(c -> validateShape(model, c).stream()).collect(Collectors.toList());
	}

	private List<ValidationEvent> validateShape(Model model, Shape shape) {
		final Map<String, Shape> members = allMembers(model, shape);
		final Map<String, Optional<ProtoIndexTrait>> fieldsAndIndexes = members.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getTrait(ProtoIndexTrait.class)));
		boolean allFieldsAreIndexed = fieldsAndIndexes.values().stream().allMatch(Optional::isPresent);
		boolean noFieldIsIndexed = fieldsAndIndexes.values().stream().allMatch(o -> !o.isPresent());
		boolean onlySomeFieldsAreIndexed = !(allFieldsAreIndexed || noFieldIsIndexed);

		if (onlySomeFieldsAreIndexed) {
			return Collections.singletonList(ValidationEvent.builder().id(INCONSISTENT_PROTO_INDEXES)
					.message("The " + ProtoIndexTrait.ID + " trait must be applied to all members of the shape.")
					.shape(shape).severity(Severity.ERROR).build());
		} else if (allFieldsAreIndexed) {
			final Map<Integer, List<String>> perIndex = fieldsAndIndexes.entrySet().stream().flatMap(
					e -> e.getValue().map(trait -> Stream.of(mapEntry(e.getKey(), trait))).orElse(Stream.empty()))
					.collect(Collectors.groupingBy(e -> e.getValue().getNumber(),
							Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

			return perIndex.entrySet().stream().filter(e -> e.getValue().size() > 1).map(e -> {
				final Integer index = e.getKey();
				final List<String> labels = e.getValue();
				final String memberNames = labels.stream().collect(Collectors.joining(","));
				return ValidationEvent
						.builder().id(DUPLICATED_PROTO_INDEX).message("Proto index " + index
								+ " is used muliple times in members " + memberNames + " of shape " + shape + ".")
						.shape(shape).severity(Severity.ERROR).build();
			}).collect(Collectors.toList());
		} else {
			return Collections.emptyList();
		}
	}

	/**
	 * Simple helper to turn deal with java.util.Map not being covariant.
	 */
	private <T extends Shape> Map.Entry<String, Shape> asShape(Map.Entry<String, T> subShape) {
		return mapEntry(subShape.getKey(), subShape.getValue());
	}

	private Optional<UnionShape> memberIsUnion(Model model, MemberShape shape) {
		return model.getShape(shape.getTarget()).flatMap(Shape::asUnionShape);
	}

	private Map<String, Shape> allMembers(Model model, Shape shape) {
		final Stream<Map.Entry<String, Shape>> unionMembers = OptionHelper.toStream(shape.asUnionShape())
				.flatMap(u -> u.getAllMembers().entrySet().stream().map(this::asShape));

		final Stream<Map.Entry<String, Shape>> enumMembers = OptionHelper.toStream(shape.asEnumShape())
				.flatMap(u -> u.getAllMembers().entrySet().stream().map(this::asShape));

		final Stream<Map.Entry<String, Shape>> structureMembers = OptionHelper.toStream(shape.asStructureShape())
				.flatMap(u -> {
					Stream<Map.Entry<String, Shape>> rootShapes = u.getAllMembers().entrySet().stream()
							.filter(e -> !memberIsUnion(model, e.getValue()).isPresent()).map(this::asShape);
					Stream<Map.Entry<String, Shape>> subUnionMembers = u.getAllMembers().values().stream()
							.flatMap(m -> OptionHelper.toStream(memberIsUnion(model, m)))
							.flatMap(union -> allMembers(model, union).entrySet().stream().map(
									e -> asShape(mapEntry(union.getId().getName() + "#" + e.getKey(), e.getValue()))));
					return Stream.concat(rootShapes, subUnionMembers);
				});
		return Stream.of(unionMembers, enumMembers, structureMembers)
				.flatMap(s -> s)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
		return new AbstractMap.SimpleEntry<K, V>(key, value);
	}
}
