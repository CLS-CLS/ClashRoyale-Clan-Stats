package org.lytsiware.clash.service.calculation.donationscore;

import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.calculation.CalculationContext;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

@Service
public class ClanDonationCalculationServiceImpl implements ClanDonationCalculationService<CalculationContext> {

    public static final String AVERAGE = "average";
    public static final String VARIANCE = "variance";

    @Override
    public CalculationContext calculateClanDonationScore(List<PlayerWeeklyStats> playersStat) {

        CalculationContext calculationContext = new CalculationContext();

        IntStream donationStrean = playersStat.stream().map(PlayerWeeklyStats::getCardDonation)
                .filter(Objects::nonNull).mapToInt(i -> i);

        Double average = donationStrean.average().orElse(0);
        Double variance = donationStrean.mapToDouble(d -> Math.pow(d - average, 2)).sum();

        calculationContext.set(AVERAGE, average);
        calculationContext.set(VARIANCE, variance);

        return calculationContext;
    }
}
