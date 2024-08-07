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

import alloy.proto.ProtoInlinedOneOfTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ProtoInlinedOneOfValidator extends AbstractValidator {
	public final static String USAGE_COUNT_EXCEEDED = "UsageCountExceeded";
	public final static String UNUSED_UNION = "UnusedUnion";

	@Override
	public List<ValidationEvent> validate(Model model) {
		return model.getUnionShapes().stream().filter(shape -> shape.hasTrait(ProtoInlinedOneOfTrait.class))
				.flatMap(unionShape -> {
					final long usageCount = model.getMemberShapes().stream()
							.filter(s -> s.getTarget().equals(unionShape.getId())).count();
					if (usageCount > 1) {
						return Stream.of(ValidationEvent.builder().id(USAGE_COUNT_EXCEEDED).message(
								"Unions annotated with @protoInlinedOneOf can only be used inside of one structure shape.")
								.shape(unionShape).severity(Severity.ERROR).build());
					} else if (usageCount == 0) {
						return Stream.of(ValidationEvent.builder().id(UNUSED_UNION)
								.message(
										"Unions annotated with @protoInlinedOneOf must be used in one structure shape.")
								.shape(unionShape).severity(Severity.ERROR).build());
					} else {
						return Stream.empty();
					}
				}).collect(Collectors.toList());
	}

}
