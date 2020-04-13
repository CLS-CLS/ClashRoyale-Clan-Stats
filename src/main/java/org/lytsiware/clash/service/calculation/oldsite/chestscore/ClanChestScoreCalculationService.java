package org.lytsiware.clash.service.calculation.oldsite.chestscore;

import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.calculation.oldsite.CalculationContext;

import java.util.List;

public interface ClanChestScoreCalculationService<T extends CalculationContext> {


    T calculateChestScore(List<PlayerWeeklyStats> playerWeeklyStats);

    void calculateCrownScore(T context);

    void calculateFinalDeviation(T context);

    void calculateDeviation(T context);

    T initContext(List<PlayerWeeklyStats> playerWeeklyStats);
}
