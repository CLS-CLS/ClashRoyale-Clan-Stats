package org.lytsiware.clash.service.calculation.donationscore;

import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.calculation.CalculationContext;

import java.util.List;

public interface ClanDonationCalculationService<T extends CalculationContext> {

    T calculateClanDonationScore(List<PlayerWeeklyStats> playersStat);

}
