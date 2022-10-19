package alloy.proto.validation;

import alloy.proto.ProtoIndexTrait;
import alloy.proto.ProtoReservedFieldsTraitValue;
import alloy.proto.ProtoReservedFieldsTraitValue.Range;
import alloy.proto.ProtoReservedFieldsTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

final public class ProtoReservedFieldsTraitValidator extends AbstractValidator {
	public final static String RESERVED_NAME_IN_STRUCTURE = "ReservedNameInStructure";
	public final static String RESERVED_NUMBER_IN_STRUCTURE = "ReservedNumberInStructure";

	@Override
	public List<ValidationEvent> validate(Model model) {
		return model.getStructureShapesWithTrait(ProtoReservedFieldsTrait.class).stream()
				.flatMap(shape -> validateShape(shape).stream()).collect(Collectors.toList());
	}

	private List<ValidationEvent> validateShape(StructureShape shape) {
		final List<ProtoReservedFieldsTraitValue> reserved = shape.getTrait(ProtoReservedFieldsTrait.class).get()
				.getReserved();

		final List<String> names = reserved.stream().flatMap(r -> r.isName() ? Stream.of(r.name) : Stream.empty())
				.collect(Collectors.toList());
		final List<Integer> numbers = reserved.stream()
				.flatMap(r -> r.isNumber() ? Stream.of(r.number) : Stream.empty()).collect(Collectors.toList());
		final List<Range> ranges = reserved.stream().flatMap(r -> r.isRange() ? Stream.of(r.range) : Stream.empty())
				.collect(Collectors.toList());

		final Map<String, Integer> fieldsAndIndexes = shape.members().stream().collect(
				Collectors.toMap(m -> m.getMemberName(), m -> m.expectTrait(ProtoIndexTrait.class).getNumber()));
		final List<ValidationEvent> checkNames = fieldsAndIndexes.keySet().stream().flatMap(name -> {
			if (names.contains(name)) {
				return Stream.of(ValidationEvent.builder().id(RESERVED_NAME_IN_STRUCTURE)
						.message("A reserved field name " + name + " was declared as a member in " + shape + ".")
						.shape(shape).severity(Severity.ERROR).build());
			} else {
				return Stream.empty();
			}
		}).collect(Collectors.toList());

		final List<ValidationEvent> checkNumbers = fieldsAndIndexes.values().stream().flatMap(n -> {
			if (numbers.contains(n)) {
				return Stream.of(ValidationEvent.builder().id(RESERVED_NUMBER_IN_STRUCTURE)
						.message("A reserved field number " + n + " was declared as in " + shape + ".").shape(shape)
						.severity(Severity.ERROR).build());
			} else {
				return Stream.empty();
			}
		}).collect(Collectors.toList());

		final List<ValidationEvent> checkRanges = fieldsAndIndexes.values().stream().flatMap(n -> {
			final boolean invalid = ranges.stream().anyMatch(r -> r.start <= n && n <= r.end);
			if (invalid) {
				return Stream.of(ValidationEvent.builder().id(RESERVED_NUMBER_IN_STRUCTURE)
						.message("A reserved field number " + n + " was declared as in " + shape + ".").shape(shape)
						.severity(Severity.ERROR).build());
			} else {
				return Stream.empty();
			}
		}).collect(Collectors.toList());

		return Stream.of(checkNames, checkNumbers, checkRanges).flatMap(Collection::stream)
				.collect(Collectors.toList());
	}
}
