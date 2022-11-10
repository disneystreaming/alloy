

package alloy.validation;

        import alloy.SimpleRestJsonTrait;
        import software.amazon.smithy.model.Model;
        import software.amazon.smithy.model.neighbor.Walker;
        import software.amazon.smithy.model.neighbor.NeighborProvider;
        import software.amazon.smithy.model.validation.AbstractValidator;
        import software.amazon.smithy.model.shapes.*;
        import software.amazon.smithy.model.traits.TimestampFormatTrait;
        import software.amazon.smithy.model.validation.ValidationEvent;

        import java.util.Optional;
        import java.util.Set;
        import java.util.List;
        import java.util.stream.Collectors;
        import java.util.stream.Stream;


public final class SimpleRestJsonTimestampValidator extends AbstractValidator {

    @Override
    public List<ValidationEvent> validate(Model model) {
        Walker walker = new Walker(NeighborProvider.of(model));
        Set<Shape> entryPoints = model.getShapesWithTrait(SimpleRestJsonTrait.class);
        Stream<Shape> closure = entryPoints.stream()
                .flatMap(restJsonService -> walker.walkShapes(restJsonService).stream());

        return closure.filter(shape -> shape.isTimestampShape() || shape.asMemberShape().isPresent() && model.getShape(shape.asMemberShape().get()
                .getTarget()).map(Shape::isTimestampShape).orElse(false))
                .flatMap(this::validateTimestamp).collect(Collectors.toList());
    }

    private Stream<ValidationEvent> validateTimestamp(Shape shape) {
        if (!shape.getTrait(TimestampFormatTrait.class).isPresent()) {
            return Stream.of(warn(shape));
        } else {
            return Stream.empty();
        }
    }

    private ValidationEvent warn(Shape shape) {
        return warning(shape, "Timestamp shape " + shape.getId() + " does not have a timestamp format trait");
    }


}
