package alloy.proto.validation;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import alloy.proto.GrpcTrait;
import alloy.validation.OptionHelper;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.*;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

final public class GrpcTraitValidator extends AbstractValidator {
	public final static String NO_OPERATION_SPECIFIED = "NoOperationSpecified";
	public final static String NO_INPUT_SHAPE_SPECIFIED = "NoInputShapeSpecified";
	public final static String NO_OUTPUT_SHAPE_SPECIFIED = "NoOutputShapeSpecified";
	public final static String ERROR_SHAPE_SPECIFIED = "ErrorShapeSpecified";

	@Override
	public List<ValidationEvent> validate(Model model) {
		return model.getServiceShapesWithTrait(GrpcTrait.class).stream()
				.flatMap(ss -> validateService(model, ss).stream()).collect(Collectors.toList());
	}

	private List<ValidationEvent> validateService(Model model, ServiceShape shape) {
		if (shape.getOperations().size() < 1) {
			return Collections.singletonList(ValidationEvent.builder().id(NO_OPERATION_SPECIFIED)
					.message("grpc service: " + shape + " needs at least one operation.").shape(shape)
					.severity(Severity.ERROR).build());
		} else {
			return shape.getOperations().stream()
					.flatMap(operationId -> OptionHelper.toStream(model.expectShape(operationId).asOperationShape()))
					.<ValidationEvent>flatMap(operation -> {
						List<ValidationEvent> inputValidation = operation.getInput()
								.map(__ -> Collections.<ValidationEvent>emptyList())
								.orElse(Collections.<ValidationEvent>singletonList(ValidationEvent.builder()
										.id(NO_INPUT_SHAPE_SPECIFIED)
										.message("An input shape must be specified on operation " + operation + ".")
										.shape(operation).severity(Severity.ERROR).build()));

						List<ValidationEvent> outputValidation = operation.getOutput()
								.map(__ -> Collections.<ValidationEvent>emptyList())
								.orElse(Collections.<ValidationEvent>singletonList(ValidationEvent.builder()
										.id(NO_OUTPUT_SHAPE_SPECIFIED)
										.message("An output shape must be specified on operation " + operation + ".")
										.shape(operation).severity(Severity.ERROR).build()));

						return Stream.of(inputValidation, outputValidation).flatMap(Collection::stream);
					}).collect(Collectors.toList());
		}
	}

}
