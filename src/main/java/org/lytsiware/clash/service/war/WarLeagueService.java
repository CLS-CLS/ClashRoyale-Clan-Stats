package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.league.WarLeague;

import java.time.LocalDate;
import java.util.List;

public interface WarLeagueService {

    WarLeague createLeague(String name, LocalDate startDate);

    WarLeague findLeagueForDate(LocalDate date, WarLeaguePhase warLeaguePhase);

}
