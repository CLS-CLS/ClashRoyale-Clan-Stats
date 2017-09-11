package org.lytsiware.clash.service;

import org.lytsiware.clash.domain.player.*;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.utils.DateWeekConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClanStatsService implements IClanStatsService {

	@Autowired
	PlayerRepository playerRepository;

	@Autowired
	PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

	@Autowired
	IClashSiteService siteService;

	@Override
	public List<PlayerOverallStats> retrieveClanStats(int week) {
		List<PlayerWeeklyStats> weeklyStats = playerWeeklyStatsRepository.findByWeek(week);
		List<PlayerOverallStats> stats = new ArrayList<>();
		weeklyStats.stream().forEach(p -> stats.add(new PlayerOverallStats(p)));
		return stats;
	}

	@Override
	public List<Player> calculateAvgs() {
		List<Player> result = new ArrayList<>();

		int startingWeek = DateWeekConverter.toWeek(LocalDate.now().minusWeeks(13));
		int latestWeek = DateWeekConverter.toWeek(LocalDate.now().minusWeeks(1));

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
	@CacheEvict(value = "playerStats", allEntries = true)
	public void updateDatabaseWithLatest() {
		List<PlayerWeeklyStats> newStats = siteService.retrieveData();

		newStats = playerWeeklyStatsRepository.saveOrUpdateAll(newStats);
		
		List<Player> updatedPlayers = calculateAvgs();

		playerRepository.saveOrUpdate(updatedPlayers);

	}

	@PostConstruct
	@Transactional
	public void initDb() {
		Random rnd = new Random();
		for (int pl = 0; pl < 10; pl++) {
			Player player = new Player("tag#" + pl, "Name_" + pl, rnd.nextInt(100), rnd.nextInt(100));
			for (int w = 1; w < 11; w++) {
				PlayerWeeklyStats stats = new PlayerWeeklyStats();
				stats.setPlayer(player);
				stats.setWeek(DateWeekConverter.toWeek(LocalDate.now().minusWeeks(w+1)));
				stats.setChestContribution(pl + w * 5);
				stats.setCardDonation(pl + w * 10);
				playerWeeklyStatsRepository.saveOrUpdate(stats);
			}
		}
	}

}
