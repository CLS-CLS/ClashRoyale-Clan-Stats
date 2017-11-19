package org.lytsiware.clash.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.job.WeekJobRepository;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.lytsiware.clash.service.integration.SiteIntegrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

@Service
public class ClanStatsServiceImpl implements ClanStatsService {

	Logger logger = LoggerFactory.getLogger(ClanStatsServiceImpl.class);

	@Autowired
	PlayerRepository playerRepository;

	@Autowired
	PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

	@Autowired
	SiteIntegrationService siteService;

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
	public List<PlayerWeeklyStats> calculateAvgs(Week toThisWeek) {
		logger.info("calculateAvgs");

		List<PlayerWeeklyStats> updatedWeeklyStats = new ArrayList<>();

		Week fromWeek = toThisWeek.minusWeeks(12);

		// finds all player stats between the provided weeks
		Map<Player, List<PlayerWeeklyStats>> allPlayerStats = playerWeeklyStatsRepository.findByWeek(fromWeek,
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
	@Caching(evict = { @CacheEvict(value = "playerStats", allEntries = true),
			@CacheEvict(value = "weeklyStats", allEntries = true), })
	public void updateDatabaseWithLatest() {
		logger.info("updateDatabaseWithLatest");

		List<PlayerWeeklyStats> newStats = siteService.retrieveData();

		playerWeeklyStatsRepository.save(newStats);

		List<PlayerWeeklyStats> playerWeeklyStats = calculateAvgs(new Week().minusWeeks(1));

		playerWeeklyStatsRepository.save(playerWeeklyStats);
	}
	

	@Override
	@Caching(evict = { @CacheEvict(value = "playerStats", allEntries = true),
			@CacheEvict(value = "weeklyStats", allEntries = true), })
	public void recalculateAndSaveAvgs(Week week) {
		List<PlayerWeeklyStats> playerWeeklyStats = calculateAvgs(week);
		playerWeeklyStatsRepository.save(playerWeeklyStats);
	}

	@Override
	public PlayerStatsDto retrievePlayerStats(String tag) {
		Player player = playerRepository.findByTag(tag);
		logger.info("tag {} -> {}", tag, player.getName());
		Week now = new Week();
		List<PlayerWeeklyStats> stats = playerWeeklyStatsRepository.findByWeeksAndTag(tag, now.minusWeeks(12),
				now.minusWeeks(1));
		return PlayerStatsDto.toPlayerStatsDto(player, stats);
	}
	
	@Override
	@Transactional(value=TxType.REQUIRED)
	public void updateOrInsertNewDonations(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly) {
		stats.stream().forEach(s -> s.setWeek(week.getWeek()));
		
		Map<String, PlayerWeeklyStats> databaseStats = playerWeeklyStatsRepository.findByWeek(week).stream()
				.collect(Collectors.toMap(s -> s.getPlayer().getTag(), Function.identity()));
		
		List<PlayerWeeklyStats> existingStats = stats.stream()
				.filter(s -> databaseStats.containsKey(s.getPlayer().getTag())).collect(Collectors.toList());
		
		List<PlayerWeeklyStats> remainingStats = new ArrayList<>(stats);
		remainingStats.removeAll(existingStats);
	
		playerWeeklyStatsRepository.updateDonations(existingStats, week, updateBiggerOnly);
		playerWeeklyStatsRepository.save(remainingStats);
	}
	
	@Override
	@Transactional(value=TxType.REQUIRED)
	public void updateChestContributions(List<PlayerWeeklyStats> stats, Week week) {
		stats.stream().forEach(s -> s.setWeek(week.getWeek()));
		playerWeeklyStatsRepository.updateChestContribution(stats, week);
	}
	
	

	@Override
	public String generateTemplate() {
		StringBuilder sb = new StringBuilder();
		Week week = new Week();
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

}
