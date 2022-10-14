package alloy.validation;

import alloy.SimpleRestJsonTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.traits.HttpHeaderTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.shapes.*;
import software.amazon.smithy.model.neighbor.Walker;
import software.amazon.smithy.model.neighbor.NeighborProvider;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SimpleRestJsonHttpHeaderValidator extends AbstractValidator {

	List<String> disallowedHeaderNames = java.util.Arrays.asList("content-type");

	@Override
	public List<ValidationEvent> validate(Model model) {
		Stream<ServiceShape> restJsonServices = model.getShapesWithTrait(SimpleRestJsonTrait.class).stream()
				.flatMap(service -> OptionHelper.toStream(service.asServiceShape()));
		Walker walker = new Walker(NeighborProvider.withTraitRelationships(model, NeighborProvider.of(model)));

		return restJsonServices.flatMap(restJsonService -> {
			Stream<Shape> allHeaderShapes = walker.walkShapes(restJsonService).stream()
					.filter(shape -> shape.hasTrait(HttpHeaderTrait.class));
			return allHeaderShapes.flatMap(headerShape -> {
				String value = headerShape.getTrait(HttpHeaderTrait.class).get().getValue();
				if (disallowedHeaderNames.contains(value.toLowerCase())) {
					return Stream.of(warning(headerShape, String
							.format("Header named `%s` may be overridden in client/server implementations", value)));
				} else {
					return Stream.empty();
				}
			});
		}).collect(Collectors.toList());
	}
}
