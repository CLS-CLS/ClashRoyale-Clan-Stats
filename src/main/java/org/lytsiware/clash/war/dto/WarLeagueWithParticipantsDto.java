package org.lytsiware.clash.war.dto;

import lombok.Data;
import org.lytsiware.clash.war.domain.league.WarLeague;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStat;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
public class WarLeagueWithParticipantsDto implements Serializable {

    List<PlayerWarStatsDto> playerWarStats;
    private int totalCards;
    private int wins;
    private int loses;
    private int forfeits;
    private int clanScore;
    private double winRatio;
    private int avgCards;
    private LocalDateTime startDate;
    private Integer rank;
    private Integer deltaTrophies;
    private Integer totalTrophies;

    public WarLeagueWithParticipantsDto(WarLeague warLeague) {
        this.playerWarStats = warLeague.getPlayerWarStats().stream()
                .filter(playerWarStat -> playerWarStat.getCollectionPhaseStats().getCardsWon() > 0)
                .map(PlayerWarStatsDto::new)
                .collect(Collectors.toList());
        this.totalCards = warLeague.getTeamTotalCards();
        this.avgCards = warLeague.getTeamCardAvg();
        this.winRatio = warLeague.getTeamWinRatio();
        this.wins = this.playerWarStats.stream().mapToInt(PlayerWarStatsDto::getWins).sum();
        this.loses = this.playerWarStats.stream().mapToInt(PlayerWarStatsDto::getLooses).sum();
        this.forfeits = this.playerWarStats.stream().mapToInt(PlayerWarStatsDto::getForfeits).sum();
        this.clanScore = warLeague.getTeamScore();
        this.startDate = warLeague.getStartDate().atTime(warLeague.getTime());
        this.deltaTrophies = warLeague.getTrophies();
        this.rank = warLeague.getRank();
        this.totalTrophies = warLeague.getTotalTrophies();
    }

    @Data
    static class PlayerWarStatsDto {
        private int cards;
        private int wins;
        private int looses;
        private int forfeits;
        private String name;
        private String tag;
        private int score;
        private List<String> fightStatus = new ArrayList<>();
        private int collectionGamesPlayed;

        public PlayerWarStatsDto(PlayerWarStat playerWarStat) {
            this.cards = playerWarStat.getCollectionPhaseStats().getCardsWon();
            this.wins = playerWarStat.getWarPhaseStats().getGamesWon();
            this.looses = playerWarStat.getWarPhaseStats().getGamesLost();
            this.forfeits = Optional.ofNullable(playerWarStat.getWarPhaseStats().getGamesNotPlayed()).orElse(0);
            this.name = playerWarStat.getPlayer().getName();
            this.tag = playerWarStat.getPlayer().getTag();
            double winRatio = playerWarStat.getWarPhaseStats().getGamesWon() / playerWarStat.getWarPhaseStats().getGamesGranted();
            this.score = (int) (playerWarStat.getCollectionPhaseStats().getCardsWon() * (0.5 + 0.5 * winRatio));
            this.collectionGamesPlayed = playerWarStat.getCollectionPhaseStats().getGamesPlayed();
            IntStream.range(0, wins).forEach(i -> fightStatus.add("win"));
            IntStream.range(0, looses).forEach(i -> fightStatus.add("loose"));
            IntStream.range(0, forfeits).forEach(i -> fightStatus.add("forfeit"));
        }
    }
}
