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

import alloy.proto.GrpcErrorTrait;
import software.amazon.smithy.model.Model;
import software.amazon.smithy.model.shapes.Shape;
import software.amazon.smithy.model.validation.AbstractValidator;
import software.amazon.smithy.model.validation.Severity;
import software.amazon.smithy.model.validation.ValidationEvent;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class GrpcErrorTraitValidator extends AbstractValidator {
    public static final String GRPC_ERROR_NON_STANDARD = "GrpcErrorNonStandard";

    @Override
    public List<ValidationEvent> validate(Model model) {
        return model.getShapesWithTrait(GrpcErrorTrait.class).stream()
                .map(shape -> validateTrait(shape, shape.expectTrait(GrpcErrorTrait.class)))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    private List<ValidationEvent> validateTrait(Shape shape, GrpcErrorTrait trait) {
        int code = trait.getCode();
        if (code < 0 || code > 16) {
            return Collections.singletonList(ValidationEvent.builder()
                    .id(GRPC_ERROR_NON_STANDARD)
                    .severity(Severity.WARNING)
                    .shape(shape)
                    .message("grpcError code is outside the standard gRPC range (0..16); many runtimes coerce unknown codes to UNKNOWN")
                    .build());
        }

        return Collections.emptyList();
    }
}
