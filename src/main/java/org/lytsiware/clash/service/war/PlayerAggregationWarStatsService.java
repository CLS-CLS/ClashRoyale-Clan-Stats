package org.lytsiware.clash.service.war;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;

import java.time.LocalDate;
import java.util.List;

public interface PlayerAggregationWarStatsService {

    void calculateStatsBetweenDates(LocalDate from, LocalDate to, int span);

    /**
     * Calculates and persists the war statistics for each player, such as the total cards won , the average cards won , the win percentage etc.
     * The wars statistics are calculated based on specific leagues that are specified by the startDate and the leagueSpan
     *
     * @param latestLeagueStartDate  the start date of the oldest league to take part in the calculation
     * @param leagueSpan the maximum number of leagues that should be part of the calculation.
     * @param strict if true the oldest league's start date should match the provided one. If false, then if the dates do not match, the oldest's league date
     *               will be used
     * @return
     * @throws IllegalArgumentException if strict is true and the start date of the oldest league does not match the provided date
     */
    List<PlayerAggregationWarStats> calculateStats(LocalDate latestLeagueStartDate, int leagueSpan, boolean strict);


    List<PlayerAggregationWarStats> calculateAndUpdateStats(LocalDate startDate, int leagueSpan, boolean strict);

    List<PlayerAggregationWarStats> findLatestWarAggregationStatsForWeek(Week week);

    void calculateMissingStats(LocalDate from, LocalDate to);

    List<PlayerAggregationWarStats> findWarStatsForPlayer(String tag, int leagueSpan, LocalDate untilDate);
}
