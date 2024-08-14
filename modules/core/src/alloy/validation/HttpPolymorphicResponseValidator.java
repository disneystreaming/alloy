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

import alloy.HttpPolymorphicResponseTrait;
import alloy.HttpSuccessTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.*;
import software.amazon.smithy.model.traits.HttpTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class HttpPolymorphicResponseValidator extends AbstractValidator {

	protected static String EXPECTED_SINGLE_MEMBER = "The httpPolymorphicResponse trait can only be used in a structure that has a single required member, and this member must target a union";
	protected static String EXPECTED_HTTP_SUCCESS_ON_ALL_MEMBER_TARGETS = "The members of a union targeted by httpPolymorphicResponse trait must all target a structure annotated with @alloy.httpSuccess";
	protected static String EXPECTED_DISTINCT_HTTP_SUCCESS = "The targets of the members of this union must have distinct httpSuccess values";

	@Override
	public List<ValidationEvent> validate(Model model) {
		return model.getMemberShapesWithTrait(HttpPolymorphicResponseTrait.class).stream().flatMap(structureMember -> {
			StructureShape container = model.expectShape(structureMember.getContainer(), StructureShape.class);
			List<ValidationEvent> errors = new ArrayList<ValidationEvent>();
			if (container.getAllMembers().size() != 1) {
				errors.add(error(container, EXPECTED_SINGLE_MEMBER));
			}
			UnionShape union = model.expectShape(structureMember.getTarget(), UnionShape.class);
			union.members().stream().collect(Collectors.groupingBy(member -> {
				return model.expectShape(member.getTarget()).getTrait(HttpSuccessTrait.class)
						.map(httpSuccess -> httpSuccess.getCode());
			})).entrySet().stream().forEach(entry -> {
				if (!entry.getKey().isPresent()) {
					errors.add(error(structureMember, EXPECTED_HTTP_SUCCESS_ON_ALL_MEMBER_TARGETS));
				} else if (entry.getValue().size() > 1) {
					Optional<Integer> statusCode = entry.getKey();
					errors.add(error(union, EXPECTED_DISTINCT_HTTP_SUCCESS));
				}
			});

			return errors.stream();
		}).collect(Collectors.toList());
	}
}
