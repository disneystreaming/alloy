package alloy.proto.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
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
				.flatMap(c -> validateShape(c).stream()).collect(Collectors.toList());
	}

	private List<ValidationEvent> validateShape(Shape shape) {
		final Map<String, MemberShape> members = allMembers(shape);
		final Map<String, Optional<ProtoIndexTrait>> fieldsAndIndexes = members.entrySet().stream().collect(
				Collectors.<Map.Entry<String, MemberShape>, String, Optional<ProtoIndexTrait>>toMap(e -> e.getKey(),
						e -> e.getValue().getTrait(ProtoIndexTrait.class)));
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
							Collectors.mapping(e -> e.getKey(), Collectors.toList())));

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

	private Map<String, MemberShape> allMembers(Shape shape) {
		return shape.asUnionShape().map(u -> u.getAllMembers())
				.orElse(shape.asStructureShape().map(u -> u.getAllMembers()).orElse(Collections.emptyMap()));
	}

	private <K, V> Map.Entry<K, V> mapEntry(K key, V value) {
		return new AbstractMap.SimpleEntry<K, V>(key, value);
	}
}
