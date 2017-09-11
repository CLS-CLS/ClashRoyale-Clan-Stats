package org.lytsiware.clash.domain.player;

import java.util.List;
import java.util.Map;

public interface PlayerWeeklyStatsRepository {
    PlayerWeeklyStats saveOrUpdate(PlayerWeeklyStats playerWeeklyStats);

    Map<Player, List<PlayerWeeklyStats>> findByWeek(int startWeek, int endWeek);

    List<PlayerWeeklyStats> findByWeek(int week);

    List<PlayerWeeklyStats> saveOrUpdateAll(List<PlayerWeeklyStats> playerWeeklyStats);
}
