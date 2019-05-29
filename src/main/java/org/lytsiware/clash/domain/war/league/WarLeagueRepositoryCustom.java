package org.lytsiware.clash.domain.war.league;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WarLeagueRepositoryCustom {

    List<WarLeague> findLeaguesAroundDate(LocalDate date, int span);

    List<WarLeague> findFirstNthWarLeaguesBeforeDate(LocalDate date, int n);

    List<WarLeague> findFirstNthWarLeaguesAfterDate(LocalDate date, int n);

    Optional<WarLeague> findLatestRecordedWarLeague();

    /**
     * Finds the Nth active war League. A WarLeague is active when has it's stats fully updated
     * (aka a warLeague with incomplete stats is not considered active).
     */
    Optional<WarLeague> findNthWarLeague(int n);

    void persistAndFlush(WarLeague warLeague);
}
