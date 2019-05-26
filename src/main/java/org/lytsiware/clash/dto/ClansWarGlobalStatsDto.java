package org.lytsiware.clash.dto;

import lombok.Data;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ClansWarGlobalStatsDto implements Serializable {

	private LocalDate latestWarRecordedDate;
	private int avgCards;
	private double avgWins;
	private int avgClanScore;

    List<PlayerWarStatsDto> playerWarStats;

	public ClansWarGlobalStatsDto(List<PlayerAggregationWarStats> playerAggregationWarStats, LocalDate latestWarRecordedDate) {
		this.latestWarRecordedDate = latestWarRecordedDate;
		this.playerWarStats = playerAggregationWarStats.stream().map(PlayerWarStatsDto::new).collect(Collectors.toList());
		int totalGamesGranted = playerWarStats.stream().mapToInt(PlayerWarStatsDto::getGamesGranted).sum();
		if (totalGamesGranted != 0) {
			avgWins = playerWarStats.stream().mapToInt(PlayerWarStatsDto::getCrownsWon).sum() / (double) totalGamesGranted;
		}
		int totalWarsParticipated = playerWarStats.stream().mapToInt(PlayerWarStatsDto::getWarsParticipated).sum();
		if (totalGamesGranted != 0) {
			avgCards = (int) playerWarStats.stream().mapToDouble(pws -> pws.getAverageCardsWon() * pws.getWarsParticipated()).sum() / totalWarsParticipated;
		}
		avgClanScore = playerAggregationWarStats.stream().mapToInt(playerWarStats -> playerWarStats.getScore() * playerWarStats.getWarsParticipated()).sum() / totalWarsParticipated;

	}

    @Data
    static class PlayerWarStatsDto implements Serializable {
        private String name;
        private String tag;
        private int numberOfWars;
        private Integer averageCardsWon;
        private int warsParticipated;
        private Integer crownsWon;
        private Integer crownsLost;
        private int gamesGranted;
        private Integer gamesNotPlayed;
        private Double winRatio;
        private Integer score;
        private boolean inClan;
        private Integer collectionGamesMissed;

        public PlayerWarStatsDto(PlayerAggregationWarStats playerAggrWarStats) {
            name = playerAggrWarStats.getPlayer().getName();
            tag = playerAggrWarStats.getPlayer().getTag();
            numberOfWars = playerAggrWarStats.getWarsEligibleForParticipation();
            warsParticipated = playerAggrWarStats.getWarsParticipated();
            averageCardsWon = playerAggrWarStats.getAvgCards();
            crownsWon = playerAggrWarStats.getGamesWon();
            gamesNotPlayed = playerAggrWarStats.getGamesNotPlayed();
            crownsLost = playerAggrWarStats.getGamesGranted() - playerAggrWarStats.getGamesWon() - playerAggrWarStats.getGamesNotPlayed();
            winRatio = playerAggrWarStats.getAvgWins();
            score = playerAggrWarStats.getScore();
            inClan = playerAggrWarStats.getPlayer().getInClan();
            gamesGranted = playerAggrWarStats.getGamesGranted();
            collectionGamesMissed = playerAggrWarStats.getCollectionGamesMissed();
        }

    }


}
