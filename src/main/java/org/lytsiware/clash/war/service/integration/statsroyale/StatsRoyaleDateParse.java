package org.lytsiware.clash.war.service.integration.statsroyale;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class StatsRoyaleDateParse {

    public LocalDateTime parseDescriptiveDate(String descriptiveDate, LocalDateTime localDateTime) throws IllegalStateException {
        log.debug("Parsing to date string {}", descriptiveDate);
        String regexExpr = "(\\d+)\\s(\\w+)";
        Pattern pattern = Pattern.compile(regexExpr);
        Matcher matcher = pattern.matcher(descriptiveDate.toLowerCase());
        Function<LocalDateTime, LocalDateTime> combiner = Function.identity();

        while (matcher.find()) {
            Integer delta = Integer.valueOf(matcher.group(1));
            KeywordMap keywordMap = KeywordMap.map(matcher.group(2)).orElseThrow(
                    () -> new IllegalArgumentException(String.format("Could not found keyword map for matcher %s", matcher.group(2))));
            combiner = combiner.andThen(keywordMap.apply(delta));
        }

        if (combiner == null) {
            throw new IllegalStateException();
        }
        return combiner.apply(localDateTime);
    }


    @Getter
    private enum KeywordMap {
        SECONDS(new String[]{"second", "seconds"}, delta -> ldt -> ldt.minusSeconds(delta)),
        MINUTES(new String[]{"minute", "minutes"}, delta -> ldt -> ldt.minusMinutes(delta)),
        HOUR(new String[]{"hour", "hours"}, delta -> ldt -> ldt.minusHours(delta)),
        DAY(new String[]{"day", "days"}, delta -> ldt -> ldt.minusDays(delta)),
        WEEK(new String[]{"week", "weeks"}, delta -> ldt -> ldt.minusWeeks(delta));

        private final String[] keywords;
        private final Function<Integer, Function<LocalDateTime, LocalDateTime>> converter;

        KeywordMap(String[] keywords, Function<Integer, Function<LocalDateTime, LocalDateTime>> converter) {
            this.keywords = keywords;
            this.converter = converter;
        }

        public static Optional<KeywordMap> map(String dateExpression) {
            return Arrays.stream(KeywordMap.values())
                    .filter(keywordMap -> Arrays.stream(keywordMap.getKeywords())
                            .anyMatch(keyword -> dateExpression.toLowerCase().contains(keyword)))
                    .findFirst();
        }

        public Function<LocalDateTime, LocalDateTime> apply(Integer value) {
            return getConverter().apply(value);
        }

    }

}
