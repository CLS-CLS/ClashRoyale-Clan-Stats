package org.lytsiware.clash.war.service;

import org.lytsiware.clash.war.domain.league.WarLeague;
import org.lytsiware.clash.war.dto.WarLeagueDto;
import org.lytsiware.clash.war.dto.WarLeagueWithParticipantsDto;

import java.time.LocalDate;
import java.util.List;

public interface WarLeagueService {

    /**
     * Calculates the averages warleague stats and returns the persisted warleague
     */
    WarLeague calculateLeagueAvgsAndSave(WarLeague warLeague);

    List<WarLeagueDto> findFirstNthWarLeagueBeforeDate(LocalDate startDate, int n);

    WarLeagueWithParticipantsDto findStatsForWarLeague(int deltaWar);
}
