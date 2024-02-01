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
import software.amazon.smithy.model.shapes.*;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.neighbor.NeighborProvider;
import software.amazon.smithy.model.neighbor.Walker;
import alloy.OpenEnumTrait;
import alloy.proto.*;
import alloy.validation.OptionHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ProtoMapKeyValidator extends AbstractValidator {
	public final static String PROTO_INVALID_MAP_KEY = "PROTO_INVALID_MAP_KEY";

	@Override
	public List<ValidationEvent> validate(Model model) {
		Walker walker = new Walker(NeighborProvider.of(model));
		Set<Shape> protoEnabledShapes = model.getShapesWithTrait(ProtoEnabledTrait.class);
		Set<Shape> grpcShapes = model.getShapesWithTrait(GrpcTrait.class);
		return Stream.concat(protoEnabledShapes.stream(), grpcShapes.stream())
				// this rule applies to protoEnabled-connected shapes
				.flatMap(shape -> walker.walkShapes(shape).stream())
				.flatMap(shape -> OptionHelper.toStream(shape.asMapShape())).flatMap(mapShape -> {
					MemberShape key = mapShape.getKey();
					Shape keyTarget = model.expectShape(key.getTarget());
					boolean keyHasWrapped = key.hasTrait(ProtoWrappedTrait.class);
					boolean keyTargetHasWrapped = keyTarget.hasTrait(ProtoWrappedTrait.class);
					if (keyHasWrapped || keyTargetHasWrapped) {
						ValidationEvent event = ValidationEvent.builder().id(PROTO_INVALID_MAP_KEY)
								.severity(Severity.ERROR)
								.message(
										"Map keys must not have the alloy.proto#protoWrapped trait, nor target shapes with that trait")
								.shape(key).build();
						return Stream.of(event);
					} else {
						return Stream.empty();
					}
				}).collect(Collectors.toList());
	}

}
