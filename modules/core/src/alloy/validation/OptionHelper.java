package alloy.validation;

import java.util.Optional;
import java.util.stream.Stream;

public class OptionHelper {
	static public <T> Stream<T> toStream(Optional<T> opt) {
		return opt.isPresent() ? Stream.of(opt.get()) : Stream.empty();
	}
}
