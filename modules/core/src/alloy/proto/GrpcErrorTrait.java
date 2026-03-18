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

package alloy.proto;

import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.model.SourceException;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.Trait;

import java.util.Optional;

public final class GrpcErrorTrait extends AbstractTrait {
    public static final ShapeId ID = ShapeId.from("alloy.proto#grpcError");

    private final int code;
    private final String message;

    public GrpcErrorTrait(int code, String message, FromSourceLocation sourceLocation) {
        super(ID, sourceLocation);
        this.code = code;
        this.message = message;
    }

    public GrpcErrorTrait(int code, String message) {
        this(code, message, SourceLocation.NONE);
    }

    public GrpcErrorTrait(int code) {
        this(code, null, SourceLocation.NONE);
    }

    public int getCode() {
        return code;
    }

    public Optional<String> getMessage() {
        return Optional.ofNullable(message);
    }

    public static final class Provider extends AbstractTrait.Provider {
        public Provider() {
            super(ID);
        }

        @Override
        public Trait createTrait(ShapeId target, Node value) {
            ObjectNode obj = value.expectObjectNode();

            Node codeNode = obj.getMember("code").orElseThrow(
                    () -> new SourceException("grpcError requires a 'code' field", value.getSourceLocation())
            );

            int code;
            if (codeNode.isNumberNode()) {
                code = codeNode.expectNumberNode().getValue().intValue();
            } else if (codeNode.isStringNode()) {
                code = parseSymbol(codeNode.expectStringNode().getValue(), codeNode.getSourceLocation());
            } else {
                throw new SourceException("grpcError 'code' must be a number or enum symbol", codeNode.getSourceLocation());
            }

            String message = obj.getStringMember("message").map(n -> n.getValue()).orElse(null);

            return new GrpcErrorTrait(code, message, value.getSourceLocation());
        }
    }

    @Override
    protected Node createNode() {
        ObjectNode.Builder builder = Node.objectNodeBuilder()
                .withMember("code", Node.from(code));
        if (message != null) {
            builder.withMember("message", Node.from(message));
        }
        return builder.build();
    }

    private static int parseSymbol(String value, SourceLocation sourceLocation) {
        switch (value) {
            case "OK":
                return 0;
            case "CANCELLED":
                return 1;
            case "UNKNOWN":
                return 2;
            case "INVALID_ARGUMENT":
                return 3;
            case "DEADLINE_EXCEEDED":
                return 4;
            case "NOT_FOUND":
                return 5;
            case "ALREADY_EXISTS":
                return 6;
            case "PERMISSION_DENIED":
                return 7;
            case "RESOURCE_EXHAUSTED":
                return 8;
            case "FAILED_PRECONDITION":
                return 9;
            case "ABORTED":
                return 10;
            case "OUT_OF_RANGE":
                return 11;
            case "UNIMPLEMENTED":
                return 12;
            case "INTERNAL":
                return 13;
            case "UNAVAILABLE":
                return 14;
            case "DATA_LOSS":
                return 15;
            case "UNAUTHENTICATED":
                return 16;
            default:
                throw new SourceException("Unknown grpcError code: " + value, sourceLocation);
        }
    }
}
