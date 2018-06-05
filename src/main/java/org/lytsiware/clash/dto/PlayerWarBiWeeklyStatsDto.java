package org.lytsiware.clash.dto;

import lombok.Data;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;

import java.io.Serializable;

@Data
public class PlayerWarBiWeeklyStatsDto implements Serializable {
    private String name;
    private String tag;
    private int numberOfWars;
    private Double averageCardsWon;
    private int warsParticipated;
    private Integer crownsWon;
    private Integer crownsLost;
    private int gamesGranted;
    private Integer gamesNotPlayed;
    private Double winRatio;
    private Integer score;
    private boolean inClan;

    public PlayerWarBiWeeklyStatsDto(PlayerAggregationWarStats playerAggrWarStats) {
        name = playerAggrWarStats.getPlayer().getName();
        tag = playerAggrWarStats.getPlayer().getTag();
        numberOfWars = playerAggrWarStats.getWarsEligibleForParticipation();
        warsParticipated = playerAggrWarStats.getWarsParticipated();
        averageCardsWon = playerAggrWarStats.getAvgCards();
        crownsWon = playerAggrWarStats.getGamesWon();
        gamesNotPlayed = playerAggrWarStats.getGamesNotPlayed();
        crownsLost = playerAggrWarStats.getGamesGranted() - playerAggrWarStats.getGamesWon();
        winRatio = playerAggrWarStats.getAvgWins();
        score = playerAggrWarStats.getScore();
        inClan = playerAggrWarStats.getPlayer().getInClan();
        gamesGranted = playerAggrWarStats.getGamesGranted();
    }

}
