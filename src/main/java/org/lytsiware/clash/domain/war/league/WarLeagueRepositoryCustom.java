package org.lytsiware.clash.domain.war.league;

import java.time.LocalDate;
import java.util.List;

public interface WarLeagueRepositoryCustom {

    List<WarLeague> findLeaguesAroundDate(LocalDate date, int span);

    List<WarLeague> findFirstNthWarLeaguesBeforeDate(LocalDate date, int n);

    List<WarLeague> findFirstNthWarLeaguesAfterDate(LocalDate date, int n);

    void persistAndFlush(WarLeague warLeague);
}
