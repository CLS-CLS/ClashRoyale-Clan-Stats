package org.lytsiware.clash.donation.service;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.donation.domain.ClanWeeklyStatRepository;
import org.lytsiware.clash.donation.domain.PlayerWeeklyStats;
import org.lytsiware.clash.donation.domain.PlayerWeeklyStatsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AggregationServiceImpl implements DonationAggregationService {

    private final Logger logger = LoggerFactory.getLogger(DonationAggregationService.class);

    private final PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    private final ClanWeeklyStatRepository clanWeeklyStatRepository;

    @Autowired
    public AggregationServiceImpl(PlayerWeeklyStatsRepository playerWeeklyStatsRepository,
                                  ClanWeeklyStatRepository clanWeeklyStatRepository) {
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.clanWeeklyStatRepository = clanWeeklyStatRepository;
    }

    @Override
    @Transactional(value = Transactional.TxType.MANDATORY)
    public void calculateAndSaveAvgs(Week week) {
        List<PlayerWeeklyStats> playerWeeklyStats = calculateAvgs(week);
        playerWeeklyStatsRepository.saveOrUpdateAll(playerWeeklyStats);
    }


    @Override
    public List<PlayerWeeklyStats> calculateAvgs(Week toThisWeek) {
        logger.info("calculateAvgs for week {}", toThisWeek);

        List<PlayerWeeklyStats> updatedWeeklyStats = new ArrayList<>();

        Week fromWeek = toThisWeek.minusWeeks(Constants.AVG_WEEKS);

        // finds all player stats between the provided weeks
        Map<Player, List<PlayerWeeklyStats>> allPlayerStats = playerWeeklyStatsRepository.findBetweenWeeks(fromWeek,
                toThisWeek);

        // finds the players that are currently in the clan
        Set<Player> currentPlayers = playerWeeklyStatsRepository.findByWeek(toThisWeek).stream()
                .map(PlayerWeeklyStats::getPlayer).collect(Collectors.toSet());

        // calculates avgs only for the players that are in the clan
        for (Player player : currentPlayers) {
            double avgCardDonation = allPlayerStats.get(player).stream().map(PlayerWeeklyStats::getCardDonation)
                    .filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(0);

            double avgChestContribution = allPlayerStats.get(player).stream().map(PlayerWeeklyStats::getChestContribution)
                    .filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(0);

            double avgCardsReceived = allPlayerStats.get(player).stream().map(PlayerWeeklyStats::getCardsReceived)
                    .filter(Objects::nonNull).mapToInt(Integer::intValue).average().orElse(0);

            PlayerWeeklyStats toUpdate = allPlayerStats.get(player).get(0);
            toUpdate.setAvgCardDonation(avgCardDonation);
            toUpdate.setAvgChestContribution(avgChestContribution);
            toUpdate.setAvgCardsReceived(avgCardsReceived);
            updatedWeeklyStats.add(toUpdate);
        }

        return updatedWeeklyStats;
    }


}
