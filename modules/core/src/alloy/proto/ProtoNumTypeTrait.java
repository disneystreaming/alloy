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

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.shapes.ShapeId;
import software.amazon.smithy.model.traits.StringTrait;

/**
 * Defines a custom serialization format for a timestamp.
 */
public final class ProtoNumTypeTrait extends StringTrait {
	public static final String SIGNED = "SIGNED";
	public static final String UNSIGNED = "UNSIGNED";
	public static final String FIXED = "FIXED";
	public static final String FIXED_SIGNED = "FIXED_SIGNED";

	public static final ShapeId ID = ShapeId.from("alloy.proto#protoNumType");

	public ProtoNumTypeTrait(String value, SourceLocation sourceLocation) {
		super(ID, value, sourceLocation);
	}

	public ProtoNumTypeTrait(String value) {
		this(value, SourceLocation.NONE);
	}

	/**
	 * Gets the {@code protoNumType} value as a {@code NumType} enum.
	 *
	 * @return Returns the {@code NumTypeF} enum.
	 */
	public NumType getNumType() {
		return NumType.fromString(getValue());
	}

	public static final class Provider extends StringTrait.Provider<ProtoNumTypeTrait> {
		public Provider() {
			super(ID, ProtoNumTypeTrait::new);
		}
	}

	/**
	 * The known {@code protoNumType} values.
	 */
	public enum NumType {
		SIGNED, UNSIGNED, FIXED, FIXED_SIGNED, UNKNOWN;

		/**
		 * Create a {@code NumType} from a string that would appear in a model.
		 *
		 * @param value Value from a trait or model.
		 * @return Returns the NumType enum value, falls back to UNKNOWN.
		 */
		public static NumType fromString(String value) {
			for (NumType numType : values()) {
				if (numType.name().equals(value)) {
					return numType;
				}
			}

			return UNKNOWN;
		}
	}
}
