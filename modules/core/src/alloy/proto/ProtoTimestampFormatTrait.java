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
public final class ProtoTimestampFormatTrait extends StringTrait {
	public static final String PROTOBUF = "PROTOBUF";
	public static final String EPOCH_MILLIS = "EPOCH_MILLIS";
	public static final String UNKNOWN = "UNKNOWN";

	public static final ShapeId ID = ShapeId.from("alloy.proto#protoTimestampFormat");

	public ProtoTimestampFormatTrait(String value, SourceLocation sourceLocation) {
		super(ID, value, sourceLocation);
	}

	public ProtoTimestampFormatTrait(String value) {
		this(value, SourceLocation.NONE);
	}

	/**
	 * Gets the {@code protoTimestampFormat} value as a {@code TimestampFormat}
	 * enum.
	 *
	 * @return Returns the {@code TimestampFormat} enum.
	 */
	public TimestampFormat getTimestampFormat() {
		return TimestampFormat.fromString(getValue());
	}

	public static final class Provider extends StringTrait.Provider<ProtoTimestampFormatTrait> {
		public Provider() {
			super(ID, ProtoTimestampFormatTrait::new);
		}
	}

	/**
	 * The known {@code protoTimestampFormat} values.
	 */
	public enum TimestampFormat {
		PROTOBUF, EPOCH_MILLIS, UNKNOWN;

		/**
		 * Create a {@code TimestampFormat} from a string that would appear in a model.
		 *
		 * @param value Value from a trait or model.
		 * @return Returns the Timestamp enum value, falls back to UNKNOWN.
		 */
		public static TimestampFormat fromString(String value) {
			for (TimestampFormat format : values()) {
				if (format.name().equals(value)) {
					return format;
				}
			}

			return UNKNOWN;
		}
	}
}
