package org.lytsiware.clash.domain.playerweeklystats;

import java.util.List;
import java.util.Map;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;

public interface PlayerWeeklyStatsRepository {
    
    Map<Player, List<PlayerWeeklyStats>> findByWeek(Week startWeek, Week endWeek);
    
    List<PlayerWeeklyStats> findByWeeksAndTag(String tag, Week startWeek, Week endWeek);
    
    List<PlayerWeeklyStats> findByWeek(Week week);
    
    void save(PlayerWeeklyStats playerWeeklyStats);

    void save(List<PlayerWeeklyStats> playerWeeklyStats);
    
    List<PlayerWeeklyStats> saveOrUpdate(List<PlayerWeeklyStats> playerWeeklyStats, Week week);

	List<PlayerWeeklyStats> updateDonations(List<PlayerWeeklyStats> donations, Week week, boolean onlyUpdateBiggerDonations);

	List<PlayerWeeklyStats> updateChestContribution(List<PlayerWeeklyStats> chestContributions, Week week);


}
