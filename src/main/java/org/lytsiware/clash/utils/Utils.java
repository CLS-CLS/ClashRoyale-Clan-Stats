package org.lytsiware.clash.utils;

import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

public class Utils {
	
	/**
	 * The {@link java.util.stream.Collectors.toMap()} throws NPE if the value is null in the key-value pair of the generated map .
	 * This method does not.
	 */
	public static <T, K,V> Collector<T, HashMap<K, V>, HashMap<K, V>> collectToMap(
			Function<T, K> keyMapper, Function<T, V> valueMapper) {

		BiConsumer<HashMap<K, V>, T> accumulator = (t, u) -> t.put(keyMapper.apply(u), valueMapper.apply(u));

		BinaryOperator<HashMap<K, V>> combiner =  (t, u) -> { t.putAll(u); return t; };

		return Collector.of(HashMap::new, accumulator, combiner);
	}

}
