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

import alloy.DataExamplesTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.NodeValidationVisitor;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.ArrayList;
import java.util.List;

public final class DataExamplesTraitValidator extends AbstractValidator {
	@Override
	public List<ValidationEvent> validate(Model model) {
		List<ValidationEvent> events = new ArrayList<>();
		for (Shape shape : model.getShapesWithTrait(DataExamplesTrait.class)) {
			DataExamplesTrait trt = shape.getTrait(DataExamplesTrait.class).get();
			for (DataExamplesTrait.DataExample example : trt.getExamples()) {
				if (example.getExampleType().equals(DataExamplesTrait.DataExampleType.SMITHY)) {
					NodeValidationVisitor visitor = createVisitor(example.getContent(), model, shape);
					events.addAll(shape.accept(visitor));
				}
			}
		}

		return events;
	}

	private NodeValidationVisitor createVisitor(Node value, Model model, Shape shape) {
		return NodeValidationVisitor.builder().model(model).eventShapeId(shape.getId()).value(value)
				.startingContext("DataExample of `" + shape.toShapeId().toString() + "`").eventId(getName()).build();
	}
}
