package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.dto.WarLeagueDto;
import org.lytsiware.clash.dto.WarLeagueWithParticipantsDto;

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
