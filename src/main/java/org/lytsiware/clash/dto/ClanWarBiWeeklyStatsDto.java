package org.lytsiware.clash.dto;

import lombok.Data;
import org.lytsiware.clash.domain.war.PlayerWarStat;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ClanWarBiWeeklyStatsDto implements Serializable {
    private String name;
    private String tag;
    private Integer numberOfWars;
    private Double averageCardsWon;
    private Integer warsParticipated;
    private Integer crownsWon;
    private Integer crownsLost;
    private Integer gamesNotPlayed;
    private Double winRatio;
    private Integer score;


    public ClanWarBiWeeklyStatsDto(List<PlayerWarStat> playerWarStats) {
        name = playerWarStats.get(0).getPlayer().getName();
        tag = playerWarStats.get(0).getPlayer().getTag();
        numberOfWars = playerWarStats.stream().map(PlayerWarStat::getWarLeague).collect(Collectors.toSet()).size();
        List<PlayerWarStat> participatedWars = playerWarStats.stream()
                .filter(pws -> pws.getCollectionPhaseStats().getCardsWon() != 0).collect(Collectors.toList());
        warsParticipated = participatedWars.size();
        averageCardsWon = playerWarStats.stream().mapToInt(pws -> pws.getCollectionPhaseStats().getCardsWon())
                .filter(i -> i != 0).average().orElse(0);
        crownsWon = participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesWon()).sum();
        gamesNotPlayed = participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesNotPlayed()).sum();
        crownsLost = gamesNotPlayed + participatedWars.stream().mapToInt(pws -> pws.getWarPhaseStats().getGamesLost()).sum();
        if (warsParticipated > 0) {
            winRatio = crownsWon / (double) (crownsLost + crownsWon);
            score = (int) ((0.75 + 0.25 * winRatio * (1 - gamesNotPlayed / (crownsWon + crownsLost))) * averageCardsWon);
        }

    }

}
