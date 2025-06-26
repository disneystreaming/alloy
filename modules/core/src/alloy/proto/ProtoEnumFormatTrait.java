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

public final class ProtoEnumFormatTrait extends StringTrait {
	public static final String ORDINAL = "ORDINAL";
	public static final String STRING_VALUE = "STRING_VALUE";
	public static final String UNKNOWN = "UNKNOWN";

	public static final ShapeId ID = ShapeId.from("alloy.proto#protoEnumFormat");

	public ProtoEnumFormatTrait(String value, SourceLocation sourceLocation) {
		super(ID, value, sourceLocation);
	}

	public ProtoEnumFormatTrait(String value) {
		this(value, SourceLocation.NONE);
	}

	public EnumFormat getEnumFormat() {
		return EnumFormat.fromString(getValue());
	}

	public static final class Provider extends StringTrait.Provider<ProtoEnumFormatTrait> {
		public Provider() {
			super(ID, ProtoEnumFormatTrait::new);
		}
	}

	public enum EnumFormat {
		ORDINAL, STRING_VALUE, UNKNOWN;

		public static EnumFormat fromString(String value) {
			for (EnumFormat format : values()) {
				if (format.name().equals(value)) {
					return format;
				}
			}

			return UNKNOWN;
		}
	}
}
