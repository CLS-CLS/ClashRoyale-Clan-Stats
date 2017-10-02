package org.lytsiware.clash.service;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;

import java.util.List;

public interface IClanStatsService {
    List<PlayerOverallStats> retrieveClanStats(Week week);

    List<PlayerWeeklyStats> calculateAvgs();

    void updateDatabaseWithLatest();

	PlayerStatsDto retrievePlayerStats(String tag);

	void recalculateAvgs();

}
