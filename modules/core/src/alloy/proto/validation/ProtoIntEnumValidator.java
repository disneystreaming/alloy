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
import software.amazon.smithy.model.shapes.IntEnumShape;
import software.amazon.smithy.model.shapes.MemberShape;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;
import software.amazon.smithy.model.neighbor.NeighborProvider;
import software.amazon.smithy.model.neighbor.Walker;
import alloy.OpenEnumTrait;
import alloy.proto.ProtoEnabledTrait;
import alloy.proto.ProtoIndexTrait;
import alloy.proto.GrpcTrait;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class ProtoIntEnumValidator extends AbstractValidator {
	public final static String PROTO_INT_ENUM_HAS_NO_ZERO = "ProtoIntEnumHasNoZero";

	@Override
	public List<ValidationEvent> validate(Model model) {
		Walker walker = new Walker(NeighborProvider.of(model));
		Set<Shape> protoEnabledShapes = model.getShapesWithTrait(ProtoEnabledTrait.class);
		Set<Shape> grpcShapes = model.getShapesWithTrait(GrpcTrait.class);
		return Stream.concat(protoEnabledShapes.stream(), grpcShapes.stream())
				// this rule applies to protoEnabled-connected shapes
				.flatMap(shape -> walker.walkShapes(shape).stream()).filter(shape -> shape.isIntEnumShape()) //
				.map(shape -> (IntEnumShape) shape)
				// this rule applies to int enums the members of which are not labelled with
				// @protoIndex
				.filter(intEnum -> !intEnum.hasTrait(OpenEnumTrait.class)).filter(intEnum -> intEnum.members().stream()
						.noneMatch(member -> member.hasTrait(ProtoIndexTrait.class)))
				.flatMap(intEnum -> {
					Collection<Integer> values = intEnum.getEnumValues().values();
					if (values.contains(0)) {
						return Stream.empty();
					} else {
						ValidationEvent error = ValidationEvent.builder().id(PROTO_INT_ENUM_HAS_NO_ZERO).shape(intEnum)
								.severity(Severity.ERROR)
								.message(
										"intEnum shape must have a 0 value when connected to a shape that has the protoEnabled trait")
								.build();
						return Stream.of(error);
					}
				}).collect(Collectors.toList());
	}

}
