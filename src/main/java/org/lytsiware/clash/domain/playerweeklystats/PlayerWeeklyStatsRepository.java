package org.lytsiware.clash.domain.playerweeklystats;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;

import java.util.List;
import java.util.Map;

public interface PlayerWeeklyStatsRepository {
    
    Map<Player, List<PlayerWeeklyStats>> findBetweenWeeks(Week startWeek, Week endWeek);
    
    List<PlayerWeeklyStats> findByWeeksAndTag(String tag, Week startWeek, Week endWeek);
    
    List<PlayerWeeklyStats> findByWeek(Week week);
    
    void save(PlayerWeeklyStats playerWeeklyStats);

    void saveOrUpdateAll(List<PlayerWeeklyStats> playerWeeklyStats);

}
