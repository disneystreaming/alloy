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
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.shapes.SimpleShape;
import software.amazon.smithy.model.shapes.StructureShape;
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
			List<String> patternParams = getParamNamesInPattern(trt.getPattern());
			StructureShape struct = model.expectShape(trt.getTarget()).asStructureShape().get();
			ArrayList<String> structureParamNames = new ArrayList<>(struct.getMemberNames());
			structureParamNames.removeAll(patternParams);

			if (!structureParamNames.isEmpty()) {
				events.add(error(patternShape, "Did not find pattern params for the following members: "
						+ String.join(", ", structureParamNames)));
			}

			struct.getAllMembers().forEach((key, value) -> {
				Shape targetShape = model.expectShape(value.getTarget());
				if (!(targetShape instanceof SimpleShape)) {
					events.add(error(patternShape,
							String.format("Pattern params must target simple shapes only, but '%s' targets '%s'", key,
									targetShape.toShapeId())));
				}
                if (!value.hasTrait(RequiredTrait.class)) {
                    events.add(error(patternShape, String.format("Pattern params must not target optional structure members, but '%s' is optional", key)));
                }
			});
		});

		return events;
	}

	private List<String> getParamNamesInPattern(String pattern) {
		Pattern pat = Pattern.compile("\\{(.*?)}");
		Matcher matcher = pat.matcher(pattern);
		ArrayList<String> results = new ArrayList<>();
		while (matcher.find()) {
			for (int j = 0; j <= matcher.groupCount(); j++) {
				results.add(matcher.group(j));
			}
		}
		return results;
	}
}
