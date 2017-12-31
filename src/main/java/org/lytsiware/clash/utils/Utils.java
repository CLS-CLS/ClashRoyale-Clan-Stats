package org.lytsiware.clash.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

	/**
	 * Rounding double to fixed decimals
	 * @param value the value to round
	 * @param places the decimals to have
	 * @return
	 */
	public static double round(double value, int places) {
		if (places < 0) throw new IllegalArgumentException();

		BigDecimal bd = new BigDecimal(Double.toString(value));
		bd = bd.setScale(places, RoundingMode.HALF_UP);
		return bd.doubleValue();
	}

}
