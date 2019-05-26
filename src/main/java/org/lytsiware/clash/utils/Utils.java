package org.lytsiware.clash.utils;

import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.WarPhaseStats;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.SimpleTriggerContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class Utils {

    /**
     * The {@link java.util.stream.Collectors#toMap(Function, Function)} throws NPE if the value is null in the key-value pair of the generated map .
     * This method does not.
     */
    public static <T, K, V> Collector<T, HashMap<K, V>, HashMap<K, V>> collectToMap(
            Function<T, K> keyMapper, Function<T, V> valueMapper) {

        BiConsumer<HashMap<K, V>, T> accumulator = (t, u) -> t.put(keyMapper.apply(u), valueMapper.apply(u));

        BinaryOperator<HashMap<K, V>> combiner = (t, u) -> {
            t.putAll(u);
            return t;
        };

        return Collector.of(HashMap::new, accumulator, combiner);
    }


    public static <T, K, V> Collector<T, HashMap<K, List<V>>, HashMap<K, List<V>>> collectToMapOfLists(
            Function<T, K> keyMapper, Function<T, V> valueMapper) {

        BiConsumer<HashMap<K, List<V>>, T> accumulator = (t, u) -> {
            K key = keyMapper.apply(u);
            if (t.containsKey(key)) {
                t.get(key).add(valueMapper.apply(u));
            } else {
                List<V> valueList = new ArrayList<>();
                valueList.add(valueMapper.apply(u));
                t.put(key, valueList);
            }
        };

        BinaryOperator<HashMap<K, List<V>>> combiner = (t, u) -> {
            for (K uKey : u.keySet()) {
                if (t.containsKey(uKey)) {
                    t.get(uKey).addAll(u.get(uKey));
                } else {
                    t.put(uKey, u.get(uKey));
                }
            }
            return t;
        };

        return Collector.of(HashMap::new, accumulator, combiner);
    }

    /**
     * Rounding double to fixed decimals
     *
     * @param value  the value to round
     * @param places the decimals to have
     * @return
     */
    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Integer parseNullableInt(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (Exception ex) {
            return null;
        }
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

    public static Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static ZonedDateTime getNextExecutionDate(String cronExpression, ZonedDateTime latestExecutionZonedDate) {
        CronTrigger cronTrigger = new CronTrigger(cronExpression, TimeZone.getTimeZone(ZoneIdConfiguration.zoneId()));
        Date lastExecutionAsDate = Utils.convertToDate(latestExecutionZonedDate, ZoneIdConfiguration.zoneId());
        Date nextExecutionAsDate = cronTrigger.nextExecutionTime(
                new SimpleTriggerContext(lastExecutionAsDate, lastExecutionAsDate, lastExecutionAsDate));
        return nextExecutionAsDate.toInstant().atZone(ZoneIdConfiguration.zoneId());
    }

    public static List<PlayerWarStat> parseCsv(InputStream inputStream, String fileName) {

        List<String> lines = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)).lines().collect(Collectors.toList());
        String leagueName = lines.get(0).split(",")[0].trim();
        String rank = lines.get(0).split(",")[1].trim();
        String trophies = lines.get(0).split(",")[2].trim();

        lines = lines.subList(2, lines.size()); //remove two first lines (league stats, and header)

        List<PlayerWarStat> statsList = new ArrayList<>();

        LocalDateTime leagueDate = LocalDateTime.parse(fileName.split("\\.")[0], DateTimeFormatter.ofPattern("dd-MM-yyyy'T'hh-mm"));

        WarLeague warLeague = new WarLeague(leagueDate);
        warLeague.setName(leagueName);
        warLeague.setRank(Integer.valueOf(rank));
        warLeague.setTrophies(Integer.valueOf(rank));

        for (String line : lines) {
            String[] stats = line.split(",");
            String tag = stats[0].trim();
            int cardsWon = Integer.parseInt(stats[1].trim());
            int gamesGranted = Integer.parseInt(stats[2].trim());
            int gamesWon = Optional.ofNullable(Utils.parseNullableInt(stats[3].trim())).orElse(0);
            int gamesLost = Optional.ofNullable(Utils.parseNullableInt(stats[4].trim())).orElse(0);

            if (gamesGranted != gamesWon) {
                throw new IllegalArgumentException("Games lost should have been provided");
            }

            if (gamesGranted != gamesLost) {
                throw new IllegalArgumentException("Games won should have been provided");
            }

            PlayerWarStat pws = PlayerWarStat.builder()
                    .player(new Player(tag, null, null, true))
                    .collectionPhaseStats(CollectionPhaseStats.builder()
                            .cardsWon(cardsWon)
                            .build())
                    .warPhaseStats(WarPhaseStats.builder()
                            .gamesWon(gamesWon)
                            .gamesLost(gamesLost)
                            .gamesGranted(gamesGranted)
                            .build())
                    .build();
            pws.setWarLeague(warLeague);
            statsList.add(pws);
        }
        return statsList;
    }

    public static void validateWarStats(String[] stats, String[] previousStats, String line) {
        if (previousStats == null) {
            return;
        }
        if (Integer.parseInt(previousStats[3].trim()) < Integer.parseInt(stats[3].trim())) {
            throw new IllegalArgumentException("Games won should not be more in the previous stat, line: " + line);
        }
        if (Integer.parseInt(previousStats[4].trim()) == Integer.parseInt(stats[4].trim()) &&
                Integer.parseInt(previousStats[2].trim()) == Integer.parseInt(stats[2].trim())
                && Integer.parseInt(previousStats[3].trim()) == Integer.parseInt(stats[3].trim())) {
            if (Integer.parseInt(previousStats[1].trim()) < Integer.parseInt(stats[1].trim())) {
                throw new IllegalArgumentException("Cards cannot be more that the previous stat, line: " + line);
            }
        }
    }

}
