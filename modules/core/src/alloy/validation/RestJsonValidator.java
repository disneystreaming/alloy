package alloy.validation;

import alloy.RestJsonTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.traits.HttpTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class RestJsonValidator extends AbstractValidator {

	@Override
	public List<ValidationEvent> validate(Model model) {
		return model.getShapesWithTrait(RestJsonTrait.class).stream().flatMap(restJson -> {
			return restJson.asServiceShape().get().getAllOperations().stream().flatMap(operationShapeId -> {
				Optional<Shape> maybeOperation = model.getShape(operationShapeId);
				Stream<ValidationEvent> emptyStream = Stream.empty();
				Optional<Stream<ValidationEvent>> result = maybeOperation.map(op -> {
					if (op.getTrait(HttpTrait.class).isPresent()) {
						return emptyStream;
					} else {
						String id = RestJsonTrait.ID.toString();
						return Stream.of(error(op,
								"Operations tied to " + id + " services must be annotated with the @http trait"));
					}
				});
				return result.orElseGet(() -> emptyStream);
			});
		}).collect(Collectors.toList());
	}
}
