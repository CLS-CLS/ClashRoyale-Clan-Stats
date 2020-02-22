package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.dto.ClansWarGlobalStatsDto;

import java.time.LocalDate;
import java.util.List;

public interface PlayerAggregationWarStatsService {

    void recalculateAndUpdateStats(WarLeague warLeagues);

    void calculateStatsBetweenDates(LocalDate from, LocalDate to, int span);

    /**
     * Calculates and persists the war statistics for each player, such as the total totalCards won , the average totalCards won , the win percentage etc.
     * The wars statistics are calculated based on specific leagues that are specified by the startDate and the leagueSpan
     *
     * @param latestLeagueStartDate the start date of the oldest league to take part in the calculation
     * @param leagueSpan            the maximum number of leagues that should be part of the calculation.
     * @return
     * @throws IllegalArgumentException if strict is true and the start date of the oldest league does not match the provided date
     */
    List<PlayerAggregationWarStats> calculateStats(LocalDate latestLeagueStartDate, int leagueSpan);

    List<PlayerAggregationWarStats> calculateAndUpdateStats(LocalDate startDate, int leagueSpan);

    ClansWarGlobalStatsDto findLatestWarAggregationStatsForWar(int deltaWar);

    List<PlayerAggregationWarStats> findWarStatsForPlayer(String tag, int leagueSpan, LocalDate untilDate);
}
