/* Copyright 2023 Disney Streaming
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

package alloy;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.node.StringNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.model.traits.TraitService;
import software.amazon.smithy.utils.ToSmithyBuilder;

public final class UrlFormNameTrait extends AbstractTrait implements ToSmithyBuilder<UrlFormNameTrait> {
    public static final ShapeId ID = ShapeId.from("alloy#urlFormName");
    private final String name;

    private UrlFormNameTrait(Builder builder) {
        super(ID, builder.getSourceLocation());
        this.name = builder.getName();
    }

    public String getName() {
        return this.name;
    }

    @Override
    protected Node createNode() {
        return ObjectNode.builder().withMember("name", name).build();
    }

    @Override
    public Builder toBuilder() {
        Builder builder = new Builder().sourceLocation(getSourceLocation());
        builder.setName(name);
        return builder;
    }

    /**
     * @return Returns a builder used to create a urlFormName trait.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractTraitBuilder<UrlFormNameTrait, Builder> {
        private String name;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public String getName() {
            return name;
        }

        @Override
        public UrlFormNameTrait build() {
            return new UrlFormNameTrait(this);
        }
    }

    public static final class Provider implements TraitService {
        @Override
        public ShapeId getShapeId() {
            return ID;
        }

        public UrlFormNameTrait createTrait(ShapeId target, Node value) {
            Builder builder = builder().sourceLocation(value);
            StringNode name = value.expectStringNode();
            builder.setName(name.getValue());
            UrlFormNameTrait result = builder.build();
            result.setNodeCache(value);
            return result;
        }
    }

}
