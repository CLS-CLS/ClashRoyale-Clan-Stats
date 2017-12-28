package org.lytsiware.clash.service.calculation;

import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;

import java.util.List;

public interface ClanChestScoreCalculationService<T extends CalculationContext> {


    double calculateChestScore(List<PlayerWeeklyStats> playerWeeklyStats);

    void calculateCrownScore(T context);

    void calculateFinalDeviation(T context);

    void calculateDeviation(T context);

    T initContext(List<PlayerWeeklyStats> playerWeeklyStats);
}
