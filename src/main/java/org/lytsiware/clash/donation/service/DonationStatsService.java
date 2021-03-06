package org.lytsiware.clash.donation.service;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.donation.dto.NewPlayersDto;
import org.lytsiware.clash.donation.dto.NewPlayersUpdateDto;
import org.lytsiware.clash.donation.dto.PlayerOverallStats;
import org.lytsiware.clash.donation.dto.PlayerStatsDto;

import java.util.List;

public interface DonationStatsService {

    List<PlayerOverallStats> retrieveClanStats(Week week);

    /**
     * Finds the players that are new to the newestWeek, compared to the players that already exist in the oldest week
     */
    NewPlayersDto findNewPlayersOfWeeks(Week oldestWeek, Week newestWeek);

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
