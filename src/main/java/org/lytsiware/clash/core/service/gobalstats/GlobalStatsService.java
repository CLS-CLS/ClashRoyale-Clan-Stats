package org.lytsiware.clash.core.service.gobalstats;

import org.lytsiware.clash.core.domain.player.GlobalPlayerStat;

import java.util.List;

public interface GlobalStatsService {

    List<GlobalPlayerStat> globalPlayerStats();
}
