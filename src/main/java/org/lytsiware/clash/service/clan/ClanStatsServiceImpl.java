package org.lytsiware.clash.service.clan;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.*;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.ClanStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ClanStatsServiceImpl implements ClanStatsService {

    private Logger logger = LoggerFactory.getLogger(ClanStatsServiceImpl.class);

    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    private AggregationService aggregationService;

    @Autowired
    public ClanStatsServiceImpl(PlayerWeeklyStatsRepository playerWeeklyStatsRepository, AggregationService aggregationService) {
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.aggregationService = aggregationService;
    }

    @Override
    public List<PlayerOverallStats> retrieveClanStats(Week week) {
        logger.info("retrieveClanStats  week: {}", week);
        return playerWeeklyStatsRepository.findByWeek(week).stream().map(PlayerOverallStats::new).collect(Collectors.toList());
    }


    @Override
    public PlayerStatsDto retrievePlayerStats(String tag, Week from, Week to) {
        List<PlayerWeeklyStats> stats = playerWeeklyStatsRepository.findByWeeksAndTag(tag, from, to);
        return PlayerStatsDto.toPlayerStatsDto(stats);
    }


    @Override
    public NewPlayersDto findNewPlayersOfWeeks(Week oldestWeek, Week newestWeek) {
        List<PlayerWeeklyStats> newestWeekPlayerStats = playerWeeklyStatsRepository.findByWeek(newestWeek);
        List<Player> oldestWeekPlayerStats = playerWeeklyStatsRepository.findByWeek(oldestWeek).stream()
                .map(PlayerWeeklyStats::getPlayer).collect(Collectors.toList());

        List<PlayerWeeklyStats> newPlayerStats = newestWeekPlayerStats.stream()
                .filter(week2stats -> !oldestWeekPlayerStats.contains(week2stats.getPlayer()))
                .collect(Collectors.toList());

        Week currentWeek = Week.now();
        Set<Player> currentPlayers = (!currentWeek.equals(newestWeek) ? playerWeeklyStatsRepository.findByWeek(currentWeek).stream().map(PlayerWeeklyStats::getPlayer).collect(Collectors.toSet()) : null);
        Predicate<PlayerWeeklyStats> stillInClan = playerWeeklyStats -> currentPlayers == null || currentPlayers.contains(playerWeeklyStats.getPlayer());

        List<NewPlayerOverallStats> overallStatsDto = newPlayerStats.stream()
                .map(playerWeeklyStats -> new NewPlayerOverallStats(playerWeeklyStats, stillInClan.test(playerWeeklyStats)))
                .collect(Collectors.toList());
        logger.debug("found new players: {}", overallStatsDto);

        return new NewPlayersDto(oldestWeek.getEndDate(), newestWeek.getEndDate(), overallStatsDto);
    }

    @Override
    @Transactional(TxType.REQUIRED)
    public List<PlayerOverallStats> resetStatsOfNewPlayers(Week week, List<NewPlayersUpdateDto> newPlayers) {
        // check that indeed these are new players
        Set<String> newPlayersTag = findNewPlayersOfWeeks(week.minusWeeks(1), week).getNewPlayers().stream()
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
                stat.setCardsReceived(null);
            }
            if (newPlayer.shouldDeleteChest()) {
                stat.setChestContribution(null);
            }

        }

        playerWeeklyStatsRepository.saveOrUpdateAll(new ArrayList<>(statsToUpdate.values()));

        // update averages from the requested week until the last week, because the averages have changed, as
        // the donations and card contributions are deleted
        Week fromWeek = week;
        while (fromWeek.getWeek() <= Week.now().previous().getWeek()) {
            logger.info("Updating averages for week {}", fromWeek);
            aggregationService.calculateAndSaveAvgs(fromWeek);
            fromWeek = fromWeek.plusWeeks(1);
        }

        return statsToUpdate.values().stream().map(PlayerOverallStats::new).collect(Collectors.toList());

    }

}