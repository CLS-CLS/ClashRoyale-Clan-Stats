package org.lytsiware.clash.service;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.dto.ClanWeeklyStatsDto;

import javax.transaction.Transactional;
import java.util.List;

public interface ClanChestScoreService {
    @Transactional(value = Transactional.TxType.REQUIRED)
    void calculateAndUpdateClanChestScore(Week week);

    List<ClanWeeklyStatsDto> getClanChestScore(Week from, Week to);
}
