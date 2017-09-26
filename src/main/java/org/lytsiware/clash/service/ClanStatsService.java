package org.lytsiware.clash.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.lytsiware.clash.domain.player.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.lytsiware.clash.service.integration.ClashStatsSiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class ClanStatsService implements IClanStatsService {

	Logger logger = LoggerFactory.getLogger(ClanStatsService.class);

	@Autowired
	PlayerRepository playerRepository;

	@Autowired
	PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

	@Autowired
    ClashStatsSiteService siteService;
	
	@Autowired
	WeekJobRepository WeekJobRepository;

	@Override
	public List<PlayerOverallStats> retrieveClanStats(Week week) {
		logger.info("retrieveClanStats  week: {}", week);
		List<PlayerWeeklyStats> weeklyStats = playerWeeklyStatsRepository.findByWeek(week);
		List<PlayerOverallStats> stats = new ArrayList<>();
		weeklyStats.stream().forEach(p -> stats.add(new PlayerOverallStats(p)));
		return stats;
	}

	@Override
	public List<Player> calculateAvgs() {
		logger.info("calculateAvgs");

		Week startingWeek = new Week().minusWeeks(13);
		Week latestWeek = new Week().minusWeeks(1);

		// finds all player stats between the provided weeks
		Map<Player, List<PlayerWeeklyStats>> allPlayerStats = playerWeeklyStatsRepository.findByWeek(startingWeek,
				latestWeek);

		// finds the players that are currently in the clan
		Set<Player> currentPlayers = playerWeeklyStatsRepository.findByWeek(latestWeek).stream()
				.map(PlayerWeeklyStats::getPlayer).collect(Collectors.toSet());

		// calculates avgs only for the players that are in the clan
		Map<Player, List<PlayerWeeklyStats>> playerStats = new HashMap<>();
		currentPlayers.stream().forEach(player -> playerStats.put(player, allPlayerStats.get(player)));

		for (Player player : playerStats.keySet()) {
			double avgCardDonation = playerStats.get(player).stream()
					.collect(Collectors.summingInt(PlayerWeeklyStats::getCardDonation))
					/ (double) playerStats.get(player).size();
			double avgChestContribution = playerStats.get(player).stream()
					.collect(Collectors.summingInt(PlayerWeeklyStats::getChestContribution))
					/ (double) playerStats.get(player).size();
			player.setAvgCardDonation(avgCardDonation);
			player.setAvgChestContribution(avgChestContribution);
		}
		return new ArrayList<>(playerStats.keySet());
	}

	@Override
	@Caching(evict = {	
			@CacheEvict(value = "playerStats", allEntries = true),
			@CacheEvict(value = "weeklyStats", allEntries = true),
			
	})
	public void updateDatabaseWithLatest() {
		logger.info("updateDatabaseWithLatest");

		List<PlayerWeeklyStats> newStats = siteService.retrieveData();

		newStats = playerWeeklyStatsRepository.saveOrUpdateAll(newStats);

		List<Player> updatedPlayers = calculateAvgs();

		playerRepository.saveOrUpdate(updatedPlayers);
		

	}
	
	

	//@PostConstruct
	@Transactional
	public void initDb() {
		logger.info("init DB");
		/*
		Random rnd = new Random();
		for (int pl = 0; pl < 10; pl++) {
			Player player = new Player("tag#" + pl, "Name_" + pl, "superman", rnd.nextInt(100), rnd.nextInt(100));
			for (int w = 1; w < 11; w++) {
				PlayerWeeklyStats stats = new PlayerWeeklyStats();
				stats.setPlayer(player);
				stats.setWeek(new Week().minusWeeks(w + 1).getWeek());
				stats.setChestContribution(pl + w * 5);
				stats.setCardDonation(pl + w * 10);
				playerWeeklyStatsRepository.saveOrUpdate(stats);
			}
		}
		*/
		updateDatabaseWithLatest();
	}

	@Override
	public PlayerStatsDto retrievePlayerStats(String tag) {
		Player player = playerRepository.findByTag(tag);
		logger.info("tag {} -> {}", tag, player.getName());
		Week now = new Week();
		List<PlayerWeeklyStats> stats = playerWeeklyStatsRepository.findByWeekAndTag(tag, now.minusWeeks(12), now.minusWeeks(1));
		return PlayerStatsDto.toPlayerStatsDto(player, stats);
	}


}
