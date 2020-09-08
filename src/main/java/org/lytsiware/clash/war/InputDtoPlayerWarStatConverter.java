package org.lytsiware.clash.war;

import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.war.domain.league.WarLeague;
import org.lytsiware.clash.war.domain.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.war.domain.playerwarstat.WarPhaseStats;
import org.lytsiware.clash.war.dto.input.WarStatsInputDto;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InputDtoPlayerWarStatConverter {


    public static List<PlayerWarStat> toPlayerWarStat(WarStatsInputDto warStatsInputDto) {
        WarLeague warLeague = new WarLeague(warStatsInputDto.getStartDate());

        warLeague.setTrophies(warStatsInputDto.getTrophies());
        warLeague.setRank(warStatsInputDto.getRank());
        warLeague.setName(warStatsInputDto.getLeagueName());

        List<WarStatsInputDto.PlayerWarStatInputDto> keptPlayers = new ArrayList<>(warStatsInputDto.getPlayerWarStats());
        keptPlayers.addAll(warStatsInputDto.getPlayersNotParticipated().stream().filter(pstats -> !pstats.isDelete()).collect(Collectors.toList()));

        for (WarStatsInputDto.PlayerWarStatInputDto playerStats : keptPlayers) {
            PlayerWarStat playerWarStat = PlayerWarStat.builder()
                    .warPhaseStats(WarPhaseStats.builder()
                            .gamesLost(playerStats.getGamesLost())
                            .gamesWon(playerStats.getGamesWon())
                            .gamesGranted(playerStats.getGamesGranted())
                            .build())
                    .collectionPhaseStats(CollectionPhaseStats.builder()
                            .gamesPlayed(playerStats.getCollectionBattlesPlayed())
                            .cardsWon(playerStats.getCards())
                            .build())
                    .player(new Player(playerStats.getTag(), playerStats.getName(), null, null))
                    .build();
            playerWarStat.setWarLeague(warLeague);
        }

        return new ArrayList<>(warLeague.getPlayerWarStats());
    }
}
