package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.PlayerWarStat;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface PlayerWarStatsService {

    List<PlayerWarStat> persistPlayerWarStats(List<PlayerWarStat> playerWarStats, LocalDate leagueStartDate, String leagueName);

    Map<Player, List<PlayerWarStat>> findAllPlayerWarStats(int numberOfPastWars, LocalDate toDate);

    String replaceNameWithTag(InputStream inputStream, String filename);

    void upload(InputStream inputStream, String fileName);
}
