package org.lytsiware.clash.utils;

import org.lytsiware.clash.ZoneIdConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

public class Utils {
	
	/**
     * The {@link java.util.stream.Collectors#toMap(Function, Function)} throws NPE if the value is null in the key-value pair of the generated map .
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

    public static Integer parseNullableInt(String integer){
        try {
            return Integer.parseInt(integer);
        } catch (Exception ex){
            return null;
        }
    }

    public static String createStatsRoyaleRefreshUrlForClanTag(String clanTag) {
        return "https://statsroyale.com/clan/" + clanTag + "/refresh";
    }

    public static Resource createStatsRoyaleForClanTag(String clanTag) {
        Resource resource;
        try {
            resource = new UrlResource("https://statsroyale.com/clan/" + clanTag);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }

        return resource;
    }

    public static Date convertToDate(ZonedDateTime localDateTime, ZoneId zoneId) {
        return Date.from(localDateTime.toInstant());
    }

    public static ZonedDateTime getNextExecutionDate(String cronExpression, ZonedDateTime latestExecutionDate) {
        CronTrigger cronTrigger = new CronTrigger(cronExpression, TimeZone.getTimeZone(ZoneIdConfiguration.zoneId()));
        Date nextExecutionAsDate = cronTrigger.nextExecutionTime(new SimpleTriggerContext(Utils.convertToDate(latestExecutionDate, ZoneIdConfiguration.zoneId()), null, null));
        ZonedDateTime nextExecution = nextExecutionAsDate.toInstant().atZone(ZoneIdConfiguration.zoneId());
        return nextExecution;
    }

}
