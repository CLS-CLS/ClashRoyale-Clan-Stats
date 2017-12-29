package org.lytsiware.clash.service;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.dto.ClanWeeklyStatsDto;
import org.lytsiware.clash.dto.NewPlayersUpdateDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;

import java.util.List;

public interface ClanStatsService {
    List<PlayerOverallStats> retrieveClanStats(Week week);

    /**
     * Calculates the averages of the past {@link Constants#MAX_PAST_WEEK} weeks (the provided week included
     * as one of these weeks)
     * @param week
     * @return
     */
    List<PlayerWeeklyStats> calculateAvgs(Week week);

    void calculateAndUpdateClanChestScore(Week week);


    List<ClanWeeklyStatsDto> getClanChestScore(Week from, Week to);

    /**
	 * Recaclulates the averages for the provided week and updates the db
	 * @param week
	 */
    void recalculateAndSaveAvgs(Week week);

   	String generateTemplate();

	/**
	 * Updates the card donation value
	 * @param stats the stats containing the players to be updated and their new card donation value
	 * @param week the week the update should  happen
	 * @param updateBiggerOnly if true the card donations are only updated if the new card donations value is bigger than the current
	 */
	void updateOrInsertNewDonations(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly);

	/**
	 * Updates the chest contributions
	 * @param stats the stats containing the players to be updated and their new chest contribution
	 * @param week the week the update should  happen
	 * @param updateBiggerOnly if true the contributions are only updated if the new contribution value is bigger than the current
	 */
	void updateChestContributions(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly);
	
	/**
	 * Finds the new players. A new player is a player that exists in compareWeek2 and does not exist in 
	 * compareWeek1
	 */
	public List<PlayerOverallStats> findNewPlayersAtWeeks(Week compareWeek1, Week compareWeek2);

	/**
	 * Resets the stats of the new players. The stats to be reset is either the chest contribution or card donation or both
	 * @param week the week the stats should be reset
	 * @param updateDto contains information of the players and the specific stats to be reset
	 * @return the updated information
	 */
	List<PlayerOverallStats> resetStatsOfNewPlayers(Week week, List<NewPlayersUpdateDto> updateDto);

	/**
	 * Retrieves the player' s statistics between the provided weeks
	 */
	PlayerStatsDto retrievePlayerStats(String tag, Week from, Week to);



}
