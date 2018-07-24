package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.PlaywerWarStatsWithAvgsDto;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PlayerWarStatsService {

    List<PlayerWarStat> persistPlayerWarStats(List<PlayerWarStat> playerWarStats);

    PlaywerWarStatsWithAvgsDto getPlayerWarStatsForWeek(String tag, LocalDate untilDate);

    WarLeague saveWarStatsWithMissingParticipants(List<PlayerWarStat> statsList);

    void updateWarStatsForAffectedLeagues(List<WarLeague> warLeagues);

    void savePlayerWarStats(List<PlayerWarStat> statsList);

    List<WarStatsInputDto.PlayerWarStatInputDto> getPlayersNotParticipated(LocalDate date, List<WarStatsInputDto.PlayerWarStatInputDto> participants);

    Map<String, Player> findPlayersNotParticipatedInWar(WarStatsInputDto playersInWar, LocalDate date);
}
