package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.WarLeague;

import java.time.LocalDate;

public interface WarLeagueService {

    WarLeague createLeague(String name, LocalDate startDate);

    WarLeague findLeagueForDate(LocalDate date, WarLeaguePhase warLeaguePhase);


}
