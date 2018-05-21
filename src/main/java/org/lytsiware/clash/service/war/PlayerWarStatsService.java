package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PlayerWarStatsService {

    List<PlayerWarStat> persistPlayerWarStats(List<PlayerWarStat> playerWarStats);

    Map<Player, List<PlayerWarStat>> findAllPlayerWarStats(int numberOfPastWars, LocalDate toDate);

}
