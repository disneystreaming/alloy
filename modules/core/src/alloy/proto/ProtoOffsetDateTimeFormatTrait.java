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
 * Defines a custom serialization format for an OffsetDateTime.
 */
public final class ProtoOffsetDateTimeFormatTrait extends StringTrait {
	public static final String PROTOBUF = "PROTOBUF";
	public static final String RFC3339_STRING = "RFC3339_STRING";

	public static final ShapeId ID = ShapeId.from("alloy.proto#protoOffsetDateTimeFormat");

	public ProtoOffsetDateTimeFormatTrait(String value, SourceLocation sourceLocation) {
		super(ID, value, sourceLocation);
	}

	public ProtoOffsetDateTimeFormatTrait(String value) {
		this(value, SourceLocation.NONE);
	}

	/**
	 * Gets the {@code protoOffsetDateTimeFormat} value as a {@code OffsetDateTimeFormat}
	 * enum.
	 *
	 * @return Returns the {@code OffsetDateTimeFormat} enum.
	 */
	public OffsetDateTimeFormat getOffsetDateTimeFormat() {
		return OffsetDateTimeFormat.fromString(getValue());
	}

	public static final class Provider extends StringTrait.Provider<ProtoOffsetDateTimeFormatTrait> {
		public Provider() {
			super(ID, ProtoOffsetDateTimeFormatTrait::new);
		}
	}

	/**
	 * The known {@code protoOffsetDateTimeFormat} values.
	 */
	public enum OffsetDateTimeFormat {
		PROTOBUF, RFC3339_STRING;

		/**
		 * Create a {@code OffsetDateTimeFormat} from a string that would appear in a model.
		 *
		 * @param value Value from a trait or model.
		 * @return Returns the OffsetDateTimeFormat enum value.
		 */
		public static OffsetDateTimeFormat fromString(String value) {
			for (OffsetDateTimeFormat format : values()) {
				if (format.name().equals(value)) {
					return format;
				}
			}

			throw new IllegalArgumentException("Unknown protoEnumFormat: " + value);
		}
	}
}
