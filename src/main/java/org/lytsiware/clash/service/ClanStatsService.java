package org.lytsiware.clash.service;

import java.util.List;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.dto.NewPlayersUpdateDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;

public interface ClanStatsService {
    List<PlayerOverallStats> retrieveClanStats(Week week);

    /**
     * Calculates the averages of the past 12 weeks (the provided week included
     * as one of the 12 weeks)
     * @param week
     * @return
     */
    List<PlayerWeeklyStats> calculateAvgs(Week week);

    void updateDatabaseWithLatest();

    void recalculateAndSaveAvgs(Week week);

    PlayerStatsDto retrievePlayerStats(String tag);
    
	String generateTemplate();

	void updateOrInsertNewDonations(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly);

	void updateChestContributions(List<PlayerWeeklyStats> stats, Week week);
	
	/**
	 * Finds the new players. A new player is a player that exists in compareWeek2 and does not exist in 
	 * compareWeek1
	 */
	public List<PlayerOverallStats> findNewPlayersAtWeeks(Week compareWeek1, Week compareWeek2);

	List<PlayerOverallStats> updateNewPlayers(Week week, List<NewPlayersUpdateDto> updateDto);



}
