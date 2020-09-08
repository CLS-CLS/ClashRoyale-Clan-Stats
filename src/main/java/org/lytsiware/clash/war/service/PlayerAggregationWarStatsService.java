package org.lytsiware.clash.war.service;

import org.lytsiware.clash.core.dto.ClansWarGlobalStatsDto;
import org.lytsiware.clash.war.domain.aggregation.PlayerAggregationWarStats;

import java.time.LocalDate;
import java.util.List;

public interface PlayerAggregationWarStatsService {


    /**
     * Calculates and persists the war aggregation stats for each player for the league at the provided start date
     *
     * @param startDate  the start date of the oldest league to take part in the calculation
     * @param leagueSpan the number of the latest leagues that should be part of the calculation.
     * @return
     */
    void calculateAndUpdateStats(LocalDate startDate, int leagueSpan);

    ClansWarGlobalStatsDto getGlobalWarStats(int deltaWar);

    List<PlayerAggregationWarStats> getWarStatsForPlayer(String tag, int leagueSpan, LocalDate untilDate);

    /**
     * Deletes the player aggregation data of the provided startdata, leagueSpan business key
     *
     * @param startDate
     * @param leagueSpan
     */
    void deleteInBatch(LocalDate startDate, int leagueSpan);
}
