package org.lytsiware.clash.service;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.dto.*;

import java.util.List;

public interface ClanStatsService {

    List<PlayerOverallStats> retrieveClanStats(Week week);

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
