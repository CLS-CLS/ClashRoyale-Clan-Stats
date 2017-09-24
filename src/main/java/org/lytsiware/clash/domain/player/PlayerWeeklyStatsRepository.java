package org.lytsiware.clash.domain.player;

import org.lytsiware.clash.Week;

import java.util.List;
import java.util.Map;

public interface PlayerWeeklyStatsRepository {
    PlayerWeeklyStats saveOrUpdate(PlayerWeeklyStats playerWeeklyStats);

    Map<Player, List<PlayerWeeklyStats>> findByWeek(Week startWeek, Week endWeek);
    
    List<PlayerWeeklyStats> findByWeekAndTag(String tag, Week startWeek, Week endWeek);

    List<PlayerWeeklyStats> findByWeek(Week week);

    List<PlayerWeeklyStats> saveOrUpdateAll(List<PlayerWeeklyStats> playerWeeklyStats);
}
