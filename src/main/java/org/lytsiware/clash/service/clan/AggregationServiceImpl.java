package org.lytsiware.clash.service.clan;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.clanweeklystats.ClanWeeklyStatRepository;
import org.lytsiware.clash.domain.clanweeklystats.ClanWeeklyStats;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.ClanWeeklyStatsDto;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.calculation.CalculationContext;
import org.lytsiware.clash.service.calculation.chestscore.ClanChestScoreCalculationService;
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

    private ClanChestScoreCalculationService clanChestScoreCalculationService;

    private ClanWeeklyStatRepository clanWeeklyStatRepository;

    @Autowired
    public AggregationServiceImpl(PlayerWeeklyStatsRepository playerWeeklyStatsRepository,
                                  ClanChestScoreCalculationService clanChestScoreCalculationService,
                                  ClanWeeklyStatRepository clanWeeklyStatRepository) {
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.clanChestScoreCalculationService = clanChestScoreCalculationService;
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

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED)
    public void calculateAndUpdateClanChestScore(Week week) {
        List<PlayerWeeklyStats> playerWeeklyStats = playerWeeklyStatsRepository.findByWeek(week);
        if (playerWeeklyStats.size() == 0) {
            return;
        }
        CalculationContext context = clanChestScoreCalculationService.calculateChestScore(playerWeeklyStats);

        double score = context.get(CalculationContext.FINAL_DEVIATION, Double.class);
        double playerDeviationSoore = context.get(CalculationContext.PLAYER_DEVIATION_PERC, Double.class);
        double crownScore = context.get(CalculationContext.CROWN_SCORE_PERC, Double.class);

        ClanWeeklyStats clanWeeklyStat = clanWeeklyStatRepository.findById(week.getWeek()).orElse(null);

        if (clanWeeklyStat == null) {
            clanWeeklyStat = new ClanWeeklyStats();
            clanWeeklyStat.setWeek(week.getWeek());
        }

        clanWeeklyStat.setClanChestScore(score);
        clanWeeklyStat.setCrownScore(crownScore);
        clanWeeklyStat.setPlayerDeviationScore(playerDeviationSoore);
        clanWeeklyStatRepository.save(clanWeeklyStat);
    }

    @Override
    public List<ClanWeeklyStatsDto> getClanChestScore(Week from, Week to) {
        List<ClanWeeklyStats> clanStats = clanWeeklyStatRepository.findByWeekBetweenOrderByWeekAsc(from.getWeek(), to.getWeek());
        List<ClanWeeklyStatsDto> clanStatsDto = clanStats.stream().map(ClanWeeklyStatsDto::from).collect(Collectors.toList());
        for (ClanWeeklyStatsDto clanWeeklyStatsDto : clanStatsDto) {
            List<Integer> data = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(clanWeeklyStatsDto.getWeek())).stream()
                    .map(PlayerWeeklyStats::getChestContribution)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());
            clanWeeklyStatsDto.setData(data);
        }
        return clanStatsDto;

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
