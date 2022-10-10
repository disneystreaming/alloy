package alloy.validation;

import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;
import alloy.DiscriminatedUnionTrait;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class DiscriminatedUnionValidator extends AbstractValidator {

	@Override
	public List<ValidationEvent> validate(Model model) {
		return model.getShapesWithTrait(DiscriminatedUnionTrait.class).stream().flatMap(unionShape -> {
			DiscriminatedUnionTrait discriminated = unionShape.getTrait(DiscriminatedUnionTrait.class).get();
			return unionShape.asUnionShape().get().getAllMembers().entrySet().stream().flatMap(entry -> {
				Optional<Shape> maybeTarget = model.getShape(entry.getValue().getTarget());
				if (maybeTarget.isPresent() && maybeTarget.get().isStructureShape()) { // if not defined then shape
																						// won't be structure
					Map<String, MemberShape> structureMembers = maybeTarget.get().asStructureShape().get()
							.getAllMembers();
					if (structureMembers.get(discriminated.getValue()) != null) {
						return Stream.of(error(entry.getValue(),
								String.format("Target of member '%s' contains discriminator '%s'", entry.getKey(),
										discriminated.getValue())));
					} else {
						return Stream.empty();
					}
				} else {
					return Stream.of(error(entry.getValue(),
							String.format("Target of member '%s' is not a structure shape", entry.getKey())));
				}
			});
		}).collect(Collectors.toList());
	}
}
