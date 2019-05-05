package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.dto.ClanWarStatsDto;
import org.lytsiware.clash.dto.WarLeagueDto;

import java.time.LocalDate;
import java.util.List;

public interface WarLeagueService {

    void calculateLeagueAvgs(WarLeague warLeague);

    WarLeague calculateLeagueAvgsAndSave(WarLeague warLeague);

    List<WarLeagueDto> findFirstNthWarLeagueBeforeDate(LocalDate startDate, int n);

    ClanWarStatsDto findStatsForWarLeague(int deltaWar);
}
