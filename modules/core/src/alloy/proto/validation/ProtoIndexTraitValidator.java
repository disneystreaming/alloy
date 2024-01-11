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
import alloy.OpenEnumTrait;
import alloy.proto.ProtoIndexTrait;
import alloy.proto.ProtoInlinedOneOfTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.*;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

final public class ProtoIndexTraitValidator extends AbstractValidator {
	public final static String INCONSISTENT_PROTO_INDEXES = "InconsistentProtoIndexes";
	public final static String DUPLICATED_PROTO_INDEX = "DuplicatedProtoIndex";
	public final static String ENUM_MUST_HAVE_ZERO = "EnumMustHaveZeroIndex";
	public final static String OPEN_ENUM_MUST_NOT_HAVE_INDEXES = "OpenEnumMustNotHaveIndexes";

	@Override
	public List<ValidationEvent> validate(Model model) {
		final Set<ShapeId> uniqueShapes = model.getMemberShapesWithTrait(ProtoIndexTrait.class).stream()
				.flatMap(ms -> findRelevantContainer(model, ms.getContainer())).collect(Collectors.toSet());
		return uniqueShapes.stream().flatMap(shapeId -> OptionHelper.toStream(model.getShape(shapeId)))
				.flatMap(c -> validateShape(model, c).stream()).collect(Collectors.toList());
	}

	/**
	 * Before validating the Shape with the @id received, we check if it's a union.
	 * If it's a union and it is annotated with @protoInlinedOneOf, we retrieve the
	 * structures (their shape ids) that references this, and add those to be
	 * validated instead.
	 */
	private Stream<ShapeId> findRelevantContainer(Model model, ShapeId id) {
		return OptionHelper.toStream(model.getShape(id)).flatMap(s -> {
			return asProtoInlinedOneOf(s).map(u -> shapeUsingUnionMembers(model, u).map(Shape::getId))
					.orElse(Stream.of(id));
		});
	}

	private Optional<UnionShape> asProtoInlinedOneOf(Shape shape) {
		return shape.asUnionShape().filter(u -> u.hasTrait(ProtoInlinedOneOfTrait.class));
	}

	private Stream<StructureShape> shapeUsingUnionMembers(Model model, UnionShape union) {
		return model.getMemberShapes().stream().flatMap(m -> {
			if (m.getTarget() == union.getId()) {
				return OptionHelper.toStream(model.getShape(m.getContainer()).flatMap(Shape::asStructureShape));
			} else {
				return Stream.empty();
			}
		});
	}

	private List<ValidationEvent> validateShape(Model model, Shape shape) {
		final Map<String, Shape> members = allMembers(model, shape);
		final Map<String, Optional<ProtoIndexTrait>> fieldsAndIndexes = members.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getTrait(ProtoIndexTrait.class)));
		final boolean allFieldsAreIndexed = fieldsAndIndexes.values().stream().allMatch(Optional::isPresent);
		final boolean noFieldIsIndexed = fieldsAndIndexes.values().stream().noneMatch(Optional::isPresent);
		final boolean onlySomeFieldsAreIndexed = !(allFieldsAreIndexed || noFieldIsIndexed);

		Stream<ValidationEvent> indexCheck = null;
		if (onlySomeFieldsAreIndexed) {
			indexCheck = Stream.of(ValidationEvent.builder().id(INCONSISTENT_PROTO_INDEXES)
					.message("The " + ProtoIndexTrait.ID + " trait must be applied to all members of the shape.")
					.shape(shape).severity(Severity.ERROR).build());
		} else if (allFieldsAreIndexed) {
			final Map<Integer, List<String>> perIndex = fieldsAndIndexes.entrySet().stream().flatMap(
					e -> e.getValue().map(trait -> Stream.of(mapEntry(e.getKey(), trait))).orElse(Stream.empty()))
					.collect(Collectors.groupingBy(e -> e.getValue().getNumber(),
							Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

			indexCheck = perIndex.entrySet().stream().filter(e -> e.getValue().size() > 1).map(e -> {
				final Integer index = e.getKey();
				final List<String> labels = e.getValue();
				final String memberNames = labels.stream().collect(Collectors.joining(","));
				return ValidationEvent
						.builder().id(DUPLICATED_PROTO_INDEX).message("Proto index " + index
								+ " is used multiple times in members " + memberNames + " of shape " + shape + ".")
						.shape(shape).severity(Severity.ERROR).build();
			});
		} else {
			indexCheck = Stream.empty();
		}

		Stream<ValidationEvent> openStringEnumChecks = null;
		if ((shape.isEnumShape() || shape.isIntEnumShape()) && shape.hasTrait(OpenEnumTrait.class)) {
			if (noFieldIsIndexed) {
				openStringEnumChecks = Stream.empty();
			} else {
				openStringEnumChecks = Stream.of(ValidationEvent.builder().id(OPEN_ENUM_MUST_NOT_HAVE_INDEXES)
						.message("Members of enumeration" + shape + "must not carry proto indexes.").shape(shape)
						.severity(Severity.ERROR).build());
			}
		} else {
			Stream.empty();
		}

		Stream<ValidationEvent> enumHasZeroChecks = null;

		if ((shape.isEnumShape() || shape.isIntEnumShape()) && !shape.hasTrait(OpenEnumTrait.class)
				&& allFieldsAreIndexed) {
			if (fieldsAndIndexes.containsValue(Optional.of(new ProtoIndexTrait(0)))) {
				enumHasZeroChecks = Stream.empty();
			} else {
				System.out.println(allFieldsAreIndexed);
				fieldsAndIndexes.entrySet().forEach(System.out::println);
				ValidationEvent event = ValidationEvent.builder().id(ENUM_MUST_HAVE_ZERO)
						.message("One of the members of enumeration" + shape
								+ "must represent a default value, with proto index set to 0.")
						.severity(Severity.ERROR).build();
				enumHasZeroChecks = Stream.of(event);
			}
		} else {
			enumHasZeroChecks = Stream.empty();
		}

		return Stream.of(indexCheck, enumHasZeroChecks, openStringEnumChecks).flatMap(s -> s)
				.collect(Collectors.toList());

	}

	/**
	 * Simple helper to turn deal with java.util.Map not being covariant.
	 */
	private <T extends Shape> Map.Entry<String, Shape> asShape(Map.Entry<String, T> subShape) {
		return mapEntry(subShape.getKey(), subShape.getValue());
	}

	private Optional<UnionShape> memberAsProtoInlinedOneOf(Model model, MemberShape shape) {
		return model.getShape(shape.getTarget()).flatMap(this::asProtoInlinedOneOf);
	}

	private Map<String, Shape> allMembers(Model model, Shape shape) {
		final Stream<Map.Entry<String, Shape>> unionMembers = OptionHelper.toStream(shape.asUnionShape())
				.flatMap(u -> u.getAllMembers().entrySet().stream().map(this::asShape));

		final Stream<Map.Entry<String, Shape>> enumMembers = OptionHelper.toStream(shape.asEnumShape())
				.flatMap(u -> u.getAllMembers().entrySet().stream().map(this::asShape));

		final Stream<Map.Entry<String, Shape>> intEnumMembers = OptionHelper.toStream(shape.asIntEnumShape())
				.flatMap(u -> u.getAllMembers().entrySet().stream().map(this::asShape));

		final Stream<Map.Entry<String, Shape>> structureMembers = OptionHelper.toStream(shape.asStructureShape())
				.flatMap(u -> {
					Stream<Map.Entry<String, Shape>> rootShapes = u.getAllMembers().entrySet().stream()
							.filter(e -> !memberAsProtoInlinedOneOf(model, e.getValue()).isPresent())
							.map(this::asShape);
					Stream<Map.Entry<String, Shape>> subUnionMembers = u.getAllMembers().values().stream()
							.flatMap(m -> OptionHelper.toStream(memberAsProtoInlinedOneOf(model, m)))
							.flatMap(union -> allMembers(model, union).entrySet().stream().map(
									e -> asShape(mapEntry(union.getId().getName() + "#" + e.getKey(), e.getValue()))));
					return Stream.concat(rootShapes, subUnionMembers);
				});
		return Stream.of(unionMembers, enumMembers, intEnumMembers, structureMembers).flatMap(s -> s)
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	private <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
		return new AbstractMap.SimpleEntry<K, V>(key, value);
	}
}
