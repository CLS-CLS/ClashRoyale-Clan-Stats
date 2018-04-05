package org.lytsiware.clash.service.clan;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.service.AggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class AggregationServiceImpl implements AggregationService {

    private Logger logger = LoggerFactory.getLogger(AggregationService.class);

    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    public AggregationServiceImpl(PlayerWeeklyStatsRepository playerWeeklyStatsRepository) {
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
    }

    @Override
    @Transactional(value = Transactional.TxType.MANDATORY)
    public void calculateAndSaveAvgs(Week week) {
        List<PlayerWeeklyStats> playerWeeklyStats = calculateAvgs(week);
        playerWeeklyStatsRepository.saveOrUpdateAll(playerWeeklyStats);
    }


    @Override
    public List<PlayerWeeklyStats> calculateAvgs(Week toThisWeek) {
        logger.info("calculateAvgs");

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

//    public List<Map<String, Double>> getDonationWeeklyScores(Week fromWeek, Week toWeek) {
//        Map<Player, List<PlayerWeeklyStats>> stats = playerWeeklyStatsRepository.findBetweenWeeks(fromWeek, toWeek);
//
//        Map<Integer, List<PlayerWeeklyStats>> statsPerWeek = stats.values().stream().flatMap(List::stream)
//                .collect(Collectors.groupingBy(PlayerWeeklyStats::getWeek));
//
//        statsPerWeek.values().stream().map(pws -> clanDonationCalculationService.calculateClanDonationScore(pws))
//                .map(context  -> {
//                    Map<String, Integer> resultMap = new HashMap<>();
//                    resultMap.put("Average", context.)
//                })
//
//
//    }
}
