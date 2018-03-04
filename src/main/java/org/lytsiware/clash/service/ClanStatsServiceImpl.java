package org.lytsiware.clash.service;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.clanweeklystats.ClanWeeklyStatRepository;
import org.lytsiware.clash.domain.clanweeklystats.ClanWeeklyStats;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.*;
import org.lytsiware.clash.service.calculation.CalculationContext;
import org.lytsiware.clash.service.calculation.ClanChestScoreCalculationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ClanStatsServiceImpl implements ClanStatsService {

    Logger logger = LoggerFactory.getLogger(ClanStatsServiceImpl.class);

    @Autowired
    PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    ClanChestScoreCalculationService clanChestScoreCalculationService;

    @Autowired
    ClanWeeklyStatRepository clanWeeklyStatRepository;

    @Override
    public List<PlayerOverallStats> retrieveClanStats(Week week) {
        logger.info("retrieveClanStats  week: {}", week);
        return playerWeeklyStatsRepository.findByWeek(week).stream().map(PlayerOverallStats::new).collect(Collectors.toList());

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

            double avgChestContribution = allPlayerStats.get(player).stream()
                    .map(PlayerWeeklyStats::getChestContribution).filter(Objects::nonNull).mapToInt(Integer::intValue)
                    .average().orElse(0);

            PlayerWeeklyStats toUpdate = allPlayerStats.get(player).get(0);
            toUpdate.setAvgCardDonation(avgCardDonation);
            toUpdate.setAvgChestContribution(avgChestContribution);
            updatedWeeklyStats.add(toUpdate);
        }

        return updatedWeeklyStats;
    }

    @Override
    public void calculateAndUpdateClanChestScore(Week week) {
        List<PlayerWeeklyStats> playerWeeklyStats = playerWeeklyStatsRepository.findByWeek(week);
        if (playerWeeklyStats.size() == 0) {
            return;
        }
        CalculationContext context = clanChestScoreCalculationService.calculateChestScore(playerWeeklyStats);

        double score = context.get(CalculationContext.FINAL_DEVIATION, Double.class);
        double playerDeviationSoore = context.get(CalculationContext.PLAYER_DEVIATION_PERC, Double.class);
        double crownScore = context.get(CalculationContext.CROWN_SCORE_PERC, Double.class);

        ClanWeeklyStats clanWeeklyStat = clanWeeklyStatRepository.findOne(week.getWeek());
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


    @Override
    @Caching(evict = {@CacheEvict(value = "playerStats", allEntries = true),
            @CacheEvict(value = "weeklyStats", allEntries = true),})
    public void recalculateAndSaveAvgs(Week week) {
        List<PlayerWeeklyStats> playerWeeklyStats = calculateAvgs(week);
        playerWeeklyStatsRepository.saveOrUpdateAll(playerWeeklyStats);
    }

    @Override
    public PlayerStatsDto retrievePlayerStats(String tag, Week from, Week to) {

        List<PlayerWeeklyStats> stats = playerWeeklyStatsRepository.findByWeeksAndTag(tag, from, to);

        return PlayerStatsDto.toPlayerStatsDto(stats);
    }

    @Transactional(value = TxType.REQUIRED)
    @Override
    public void updateOrInsertDonationAndContributions(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly) {
        logger.info("Update/insert weekly stats for week {} , bigger-only={}", week.getWeek(), updateBiggerOnly);
        Map<String, PlayerWeeklyStats> dbStatsPerTag = playerWeeklyStatsRepository.findByWeek(week).stream().collect(Collectors.toMap(pws -> pws.getPlayer().getTag(), Function.identity()));

        List<PlayerWeeklyStats> toUpdate = new ArrayList<>();

        for (PlayerWeeklyStats newStat : stats) {
            PlayerWeeklyStats dbStat = dbStatsPerTag.get(newStat.getPlayer().getTag());
            if (dbStat != null) {
                if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getChestContribution(), dbStat.getChestContribution()) == 1) {
                    logger.info("update contribution for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getChestContribution(),
                            newStat.getChestContribution());
                    dbStat.setChestContribution(newStat.getChestContribution());
                }
                if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getCardDonation(), dbStat.getCardDonation()) == 1) {
                    logger.info("update donation for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getCardDonation(), newStat.getCardDonation());
                    dbStat.setCardDonation(newStat.getCardDonation());
                }
            } else {
                Player newPlayer = new Player(newStat.getPlayer().getTag(), newStat.getPlayer().getName(), newStat.getPlayer().getRole());
                dbStat = new PlayerWeeklyStats(newPlayer, week.getWeek(), newStat.getChestContribution(), newStat.getCardDonation(), 0, 0);
                dbStat.setWeek(week.getWeek());
                logger.info("add new player {} with chestContribution  {} and donation {}", dbStat.getPlayer().getName(), dbStat.getChestContribution(),
                        dbStat.getCardDonation());
            }
            toUpdate.add(dbStat);
        }
        playerWeeklyStatsRepository.saveOrUpdateAll(toUpdate);
    }

    @Override
    @Transactional(value = TxType.REQUIRED)
    public void updateOrInsertNewDonationsAndRole(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly) {
        logger.info("Update/insert card donations for week {} , bigger-only={}", week.getWeek(), updateBiggerOnly);
        Map<String, PlayerWeeklyStats> dbStatsPerTag = playerWeeklyStatsRepository.findByWeek(week).stream().collect(Collectors.toMap(pws -> pws.getPlayer().getTag(), Function.identity()));

        List<PlayerWeeklyStats> toUpdate = new ArrayList<>();

        for (PlayerWeeklyStats newStat : stats) {
            PlayerWeeklyStats dbStat = dbStatsPerTag.get(newStat.getPlayer().getTag());
            if (dbStat != null) {
                dbStat.getPlayer().setRole(newStat.getPlayer().getRole());
                if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getCardDonation(), dbStat.getCardDonation()) == 1) {
                    logger.info("update donation for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getCardDonation(), newStat.getCardDonation());
                    dbStat.setCardDonation(newStat.getCardDonation());
                }
            } else {
                Player newPlayer = new Player(newStat.getPlayer().getTag(), newStat.getPlayer().getName(), newStat.getPlayer().getRole());
                dbStat = new PlayerWeeklyStats(newPlayer, week.getWeek(), null, newStat.getCardDonation(), 0, 0);
                dbStat.setWeek(week.getWeek());
                logger.info("add new player {} with donation {}", dbStat.getPlayer().getName(), dbStat.getCardDonation());
            }
            toUpdate.add(dbStat);
        }
        playerWeeklyStatsRepository.saveOrUpdateAll(toUpdate);
    }

    @Override
    @Transactional(value = TxType.REQUIRED)
    public void updateChestContibutionAndRole(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly) {
        logger.info("Update chest contributions for week {} , bigger-only={}", week.getWeek(), updateBiggerOnly);
        Map<String, PlayerWeeklyStats> dbStatsPerTag = playerWeeklyStatsRepository.findByWeek(week).stream().collect(Collectors.toMap(pws -> pws.getPlayer().getTag(), Function.identity()));

        List<PlayerWeeklyStats> toUpdate = new ArrayList<>();

        for (PlayerWeeklyStats newStat : stats) {
            PlayerWeeklyStats dbStat = dbStatsPerTag.get(newStat.getPlayer().getTag());
            if (dbStat == null) {
                continue;
            }
            dbStat.getPlayer().setRole(newStat.getPlayer().getRole());
            if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getChestContribution(), dbStat.getChestContribution()) == 1) {
                logger.info("update contribution for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getChestContribution(),
                        newStat.getChestContribution());
                dbStat.setChestContribution(newStat.getChestContribution());
            }
            toUpdate.add(dbStat);
        }
        playerWeeklyStatsRepository.saveOrUpdateAll(toUpdate);
    }


    @Override
    public String generateTemplate() {
        StringBuilder sb = new StringBuilder();
        Week week = Week.now();
        List<PlayerWeeklyStats> result = playerWeeklyStatsRepository.findByWeek(week);
        if (result.isEmpty()) {
            week = week.minusWeeks(1);
            result = playerWeeklyStatsRepository.findByWeek(week);
        }
        if (result.isEmpty()) {
            week = week.minusWeeks(2);
            result = playerWeeklyStatsRepository.findByWeek(week);
        }
        week = week.plusWeeks(1);
        sb.append("WEEK=").append(week.getWeek()).append(" //this week is from monday ").append(week.getStartDate())
                .append("\r\n");
        sb.append("TAG, NAME, RANK, DONATIONS, CHEST_CONTR").append("\r\n");
        result.stream()
                .forEach(p -> sb.append(p.getPlayer().getTag() + ",") //
                        .append(p.getPlayer().getName() + ", ") //
                        .append(p.getPlayer().getRole() + ", ") //
                        .append(p.getCardDonation() + ", ") //
                        .append(p.getChestContribution()) //
                        .append("\r\n"));
        return sb.toString();

    }

    @Override
    public NewPlayersDto findNewPlayersAtWeeks(Week oldestWeek, Week newestWeek) {
        List<PlayerWeeklyStats> newestWeekPlayerStats = playerWeeklyStatsRepository.findByWeek(newestWeek);
        List<Player> oldestWeekPlayerStats = playerWeeklyStatsRepository.findByWeek(oldestWeek).stream()
                .map(PlayerWeeklyStats::getPlayer).collect(Collectors.toList());

        List<PlayerWeeklyStats> newPlayerStats = newestWeekPlayerStats.stream()
                .filter(week2stats -> !oldestWeekPlayerStats.contains(week2stats.getPlayer()))
                .collect(Collectors.toList());

        List<PlayerOverallStats> overallStatsDto = newPlayerStats.stream().map(PlayerOverallStats::new)
                .collect(Collectors.toList());
        logger.debug("found new players: {}", overallStatsDto);

        return new NewPlayersDto(oldestWeek.getEndDate(), newestWeek.getEndDate(), overallStatsDto);
    }

    @Override
    public List<PlayerOverallStats> resetStatsOfNewPlayers(Week week, List<NewPlayersUpdateDto> newPlayers) {
        // check that indeed these are new players
        Set<String> newPlayersTag = findNewPlayersAtWeeks(week.minusWeeks(1), week).getNewPlayers().stream()
                .map(PlayerOverallStats::getTag).collect(Collectors.toSet());
        Set<String> toUpdate = newPlayers.stream().map(NewPlayersUpdateDto::getTag).collect(Collectors.toSet());

        if (!newPlayersTag.containsAll(toUpdate)) {
            throw new IllegalStateException("The dto contains players that are not new");
        }

        List<PlayerWeeklyStats> allWeeklyStats = playerWeeklyStatsRepository.findByWeek(week);

        Map<String, PlayerWeeklyStats> statsToUpdate = allWeeklyStats.stream()
                .filter(pws -> toUpdate.contains(pws.getPlayer().getTag()))
                .collect(Collectors.toMap(pws -> pws.getPlayer().getTag(), Function.identity()));

        for (NewPlayersUpdateDto newPlayer : newPlayers) {
            PlayerWeeklyStats stat = statsToUpdate.get(newPlayer.getTag());
            if (newPlayer.shouldDeleteCard()) {
                stat.setCardDonation(null);
            }
            if (newPlayer.shouldDeleteChest()) {
                stat.setChestContribution(null);
            }

        }

        playerWeeklyStatsRepository.saveOrUpdateAll(new ArrayList<>(statsToUpdate.values()));

        // update averages from the requested week until the last week, because the averages have changed, as
        // the donations and card contributions
        // are deleted
        Week fromWeek = week;
        while (fromWeek.getWeek() <= Week.now().previous().getWeek()) {
            logger.info("Updating averages for week {}", fromWeek);
            recalculateAndSaveAvgs(fromWeek);
            fromWeek = fromWeek.plusWeeks(1);
        }

        return statsToUpdate.values().stream().map(PlayerOverallStats::new).collect(Collectors.toList());

    }

}
