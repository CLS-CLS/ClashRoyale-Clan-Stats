package org.lytsiware.clash.domain.playerweeklystats;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Transactional(propagation = Propagation.MANDATORY)
public interface PlayerWeeklyStatsRepository {

    Map<Player, List<PlayerWeeklyStats>> findBetweenWeeks(Week startWeek, Week endWeek);

    List<PlayerWeeklyStats> findByWeeksAndTag(String tag, Week startWeek, Week endWeek);

    List<PlayerWeeklyStats> findByWeek(Week week);

    void save(PlayerWeeklyStats playerWeeklyStats);

    void saveOrUpdateAll(List<PlayerWeeklyStats> playerWeeklyStats);

}
