package org.lytsiware.clash.service;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.dto.*;

import javax.transaction.Transactional;
import java.util.List;

public interface ClanStatsService {
    List<PlayerOverallStats> retrieveClanStats(Week week);

    /**
     * Calculates the averages of the past {@link Constants#AVG_WEEKS weeks (the provided week included
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
	 * Updates the card donation and chest contribution values
	 * @param stats the stats containing the players to be updated and thei new value
	 * @param week the week the update should happen
	 * @param updateBiggerOnly if true then each value is only updated if the new value is bigger than the current one
	 */
	void updateOrInsertDonationAndContributions(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly);

	/**
	 * Updates the card donation value
	 * @param stats the stats containing the players to be updated and their new card donation value
	 * @param week the week the update should  happen
	 * @param updateBiggerOnly if true the card donations are only updated if the new card donations value is bigger than the current
	 */
	void updateOrInsertNewDonationsAndRole(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly);

	/**
	 * Updates the chest contributions
	 * @param stats the stats containing the players to be updated and their new chest contribution
	 * @param week the week the update should  happen
	 * @param updateBiggerOnly if true the contributions are only updated if the new contribution value is bigger than the current.
	 * if the current contribution is null then it is only updated if the new one is bigger than 0. A null current contribution, indicates
	 * that the player has joined after the chan chest was started
	 */
	void updateChestContibutionAndRole(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly);
	
	/**
	 * Finds the new players. A new player is a player that exists in compareWeek2 and does not exist in 
	 * compareWeek1
	 */
	NewPlayersDto findNewPlayersAtWeeks(Week compareWeek1, Week compareWeek2);

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
