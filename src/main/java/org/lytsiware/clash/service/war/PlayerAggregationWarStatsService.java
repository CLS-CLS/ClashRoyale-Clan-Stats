package org.lytsiware.clash.service.war;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface PlayerAggregationWarStatsService {

    /**
     * Calculates and persists the war statistics for each player, such as the total cards won , the average cards won , the win percentage etc.
     * The wars statistics are calculated based on specific leagues that are specified by the stardDate and the leagueSpan
     *
     * @param latestLeagueStartDate  the start date of the oldest league to take part in the calculation
     * @param leagueSpan the maximum number of leagues that should be part of the calculation.
     * @param strict if true the olderst league's start date should match the provided one. If false, then if the dates do not match, the oldest's league date
     *               will be used
     * @return
     * @throws IllegalArgumentException if strict is true and the start date of the oldest league does not match the provided date
     */
    List<PlayerAggregationWarStats> calculateStats(LocalDate latestLeagueStartDate, int leagueSpan, boolean strict);


    CompletableFuture<List<PlayerAggregationWarStats>> calculateAndSaveStats(LocalDate startDate, int leagueSpan, boolean strict);

    List<PlayerAggregationWarStats> findLatestWarAggregationStatsForWeek(Week week);

    void calculateMissingStats(LocalDate from, LocalDate to);
}
