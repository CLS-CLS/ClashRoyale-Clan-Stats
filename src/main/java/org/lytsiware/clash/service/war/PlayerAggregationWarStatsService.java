package org.lytsiware.clash.service.war;

import org.springframework.scheduling.annotation.Async;

import java.time.LocalDate;
import java.util.concurrent.Future;

public interface PlayerAggregationWarStatsService {
    /**
     * Calculates and persists the war statistics for each player, such as the total cards won , the average cards won , the win percentage etc.
     * The wars statistics are calculated based on specific leagues that are specified by the stardDate and the leagueSpan
     *
     * @param startDate  the start date of the oldest league to take part in the calculation
     * @param leagueSpan the maximum number of leagues that should be part of the calculation.
     * @return
     */
    Future calculateStats(LocalDate startDate, int leagueSpan);
}
