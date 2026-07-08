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

import alloy.StructurePatternTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.DocumentShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.SimpleShape;
import software.amazon.smithy.model.shapes.StringShape;
import software.amazon.smithy.model.shapes.StructureShape;
import software.amazon.smithy.model.shapes.UnionShape;
import software.amazon.smithy.model.traits.RequiredTrait;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class StructurePatternTraitValidator extends AbstractValidator {

	@Override
	public List<ValidationEvent> validate(Model model) {
		List<ValidationEvent> events = new ArrayList<>();

		model.getStringShapesWithTrait(StructurePatternTrait.class).forEach(patternShape -> {
			StructurePatternTrait trt = patternShape.expectTrait(StructurePatternTrait.class);
			Shape targetShape = model.expectShape(trt.getTarget());

			if (targetShape instanceof StructureShape) {
				validateStructureTarget(model, patternShape, trt, (StructureShape) targetShape, events);
			} else if (targetShape instanceof UnionShape) {
				validateUnionTarget(model, patternShape, trt, (UnionShape) targetShape, events);
			}

			if (trt.getPattern().contains("}{")) {
				events.add(error(patternShape, "Params must be separated by at least one character"));
			}
		});

		return events;
	}

	private void validateStructureTarget(Model model, StringShape patternShape, StructurePatternTrait trt,
			StructureShape struct, List<ValidationEvent> events) {
		List<String> patternParams = getParamNamesInPattern(trt.getPattern());
		ArrayList<String> structureParamNames = new ArrayList<>(struct.getMemberNames());
		structureParamNames.removeAll(patternParams);

		if (!structureParamNames.isEmpty()) {
			events.add(error(patternShape, "Did not find pattern params for the following members: "
					+ String.join(", ", structureParamNames)));
		}

		struct.getAllMembers().forEach((key, value) -> {
			Shape memberTarget = model.expectShape(value.getTarget());
			if (!(memberTarget instanceof SimpleShape) || memberTarget instanceof DocumentShape) {
				events.add(error(patternShape,
						String.format("Pattern params must target simple shapes only, but '%s' targets '%s'", key,
								memberTarget.toShapeId())));
			}
			if (!value.hasTrait(RequiredTrait.class)) {
				events.add(error(patternShape, String.format(
						"Pattern params must not target optional structure members, but '%s' is optional", key)));
			}
		});
	}

	private void validateUnionTarget(Model model, StringShape patternShape, StructurePatternTrait trt,
			UnionShape union, List<ValidationEvent> events) {
		List<String> patternParams = getParamNamesInPattern(trt.getPattern());

		if (patternParams.size() != 2 || !patternParams.contains("label") || !patternParams.contains("value")) {
			events.add(error(patternShape,
					"When target is a union, the pattern must contain exactly '{label}' and '{value}'"));
			return;
		}

		union.getAllMembers().forEach((key, value) -> {
			Shape memberTarget = model.expectShape(value.getTarget());
			if (!(memberTarget instanceof SimpleShape) || memberTarget instanceof DocumentShape) {
				events.add(error(patternShape,
						String.format(
								"Union members must target simple shapes (excluding document), but '%s' targets '%s', which is a '%s'",
								key, memberTarget.toShapeId(), memberTarget.getType())));
			}
		});

	}

	private List<String> getParamNamesInPattern(String pattern) {
		Pattern pat = Pattern.compile("\\{(.*?)}");
		Matcher matcher = pat.matcher(pattern);
		ArrayList<String> results = new ArrayList<>();
		while (matcher.find()) {
			results.add(matcher.group(1));
		}
		return results;
	}
}
