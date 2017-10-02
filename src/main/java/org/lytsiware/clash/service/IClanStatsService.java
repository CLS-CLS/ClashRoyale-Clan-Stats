package org.lytsiware.clash.service;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;

import java.util.List;

public interface IClanStatsService {
    List<PlayerOverallStats> retrieveClanStats(Week week);

    List<Player> calculateAvgs();

    void updateDatabaseWithLatest();

	PlayerStatsDto retrievePlayerStats(String tag);

	void recalculateAvgs();

}
