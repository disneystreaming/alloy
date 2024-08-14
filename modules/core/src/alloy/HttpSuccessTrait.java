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

import software.amazon.smithy.model.FromSourceLocation;
import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.NumberNode;
import software.amazon.smithy.model.traits.AbstractTrait;
import software.amazon.smithy.model.traits.Trait;
import software.amazon.smithy.model.shapes.ShapeId;

public final class HttpSuccessTrait extends AbstractTrait {
	public static final ShapeId ID = ShapeId.from("alloy#httpSuccess");

	private final int code;

	public HttpSuccessTrait(int code, FromSourceLocation sourceLocation) {
		super(ID, sourceLocation);
		this.code = code;
	}

	public HttpSuccessTrait(int code) {
		this(code, SourceLocation.NONE);
	}

	public static final class Provider extends AbstractTrait.Provider {
		public Provider() {
			super(ID);
		}

		@Override
		public Trait createTrait(ShapeId target, Node value) {
			return new HttpSuccessTrait(value.expectNumberNode().getValue().intValue(), value.getSourceLocation());
		}
	}

	public int getCode() {
		return code;
	}

	@Override
	protected Node createNode() {
		return new NumberNode(code, getSourceLocation());
	}
}
