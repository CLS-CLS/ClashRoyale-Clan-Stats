package org.lytsiware.clash.service;

import org.lytsiware.clash.domain.player.GlobalPlayerStat;

import java.util.List;

public interface GlobalStatsService {

    List<GlobalPlayerStat> globalPlayerStats();
}
