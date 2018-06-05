package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.PlaywerWarStatsWithAvgsDto;

import java.time.LocalDate;
import java.util.List;

public interface PlayerWarStatsService {

    List<PlayerWarStat> persistPlayerWarStats(List<PlayerWarStat> playerWarStats);


    PlaywerWarStatsWithAvgsDto getPlayerWarStatsForWeek(String tag, LocalDate untilDate);
}
