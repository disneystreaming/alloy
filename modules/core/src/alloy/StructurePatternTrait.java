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

package alloy;

import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.AbstractTraitBuilder;
import software.amazon.smithy.model.traits.TraitService;
import software.amazon.smithy.utils.ToSmithyBuilder;

public final class StructurePatternTrait extends AbstractTrait implements ToSmithyBuilder<StructurePatternTrait> {
    public static final ShapeId ID = ShapeId.from("alloy#structurePattern");
    private final ShapeId target;
    private final String pattern;

    private StructurePatternTrait(Builder builder) {
        super(ID, builder.getSourceLocation());
        this.target = builder.getTarget();
        this.pattern = builder.getPattern();
    }

    public ShapeId getTarget() {
        return this.target;
    }

    public String getPattern() {
        return this.pattern;
    }

    @Override
    protected Node createNode() {
        return ObjectNode.builder().withMember("target", target.toString()).withMember("pattern", pattern).build();
    }

    @Override
    public Builder toBuilder() {
        Builder builder = new Builder().sourceLocation(getSourceLocation());
        builder.setTarget(target);
        builder.setPattern(pattern);
        return builder;
    }

    /**
     * @return Returns a builder used to create an examples trait.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder extends AbstractTraitBuilder<StructurePatternTrait, Builder> {
        private ShapeId target;
        private String pattern;

        public Builder setTarget(ShapeId target) {
            this.target = target;
            return this;
        }

        public Builder setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public ShapeId getTarget() {
            return target;
        }

        public String getPattern() {
            return pattern;
        }

        @Override
        public StructurePatternTrait build() {
            return new StructurePatternTrait(this);
        }
    }

    public static final class Provider implements TraitService {
        @Override
        public ShapeId getShapeId() {
            return ID;
        }

        public StructurePatternTrait createTrait(ShapeId target, Node value) {
            Builder builder = builder().sourceLocation(value);
            ObjectNode on = value.expectObjectNode();
            String pattern = on.expectStringMember("pattern").getValue();
            ShapeId tar = on.expectMember("target").expectStringNode().expectShapeId();
            builder.setPattern(pattern);
            builder.setTarget(tar);
            StructurePatternTrait result = builder.build();
            result.setNodeCache(value);
            return result;
        }
    }

}
