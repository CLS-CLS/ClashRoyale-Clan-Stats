package org.lytsiware.clash.service;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.util.List;

public interface IClanStatsService {
    List<PlayerOverallStats> retrieveClanStats(Week week);

    /**
     * Calculates the averages of the past 12 weeks (the provided week included
     * as one of the 12 weeks)
     * @param week
     * @return
     */
    List<PlayerWeeklyStats> calculateAvgs(Week week);

    void updateDatabaseWithLatest();

    void recalculateAvgs(Week week);

    PlayerStatsDto retrievePlayerStats(String tag);


}
