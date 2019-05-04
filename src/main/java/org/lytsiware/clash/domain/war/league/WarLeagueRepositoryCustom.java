package org.lytsiware.clash.domain.war.league;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WarLeagueRepositoryCustom {

    List<WarLeague> findLeaguesAroundDate(LocalDate date, int span);

    List<WarLeague> findFirstNthWarLeaguesBeforeDate(LocalDate date, int n);

    List<WarLeague> findFirstNthWarLeaguesAfterDate(LocalDate date, int n);

    Optional<WarLeague> findLatestRecordedWarLeague();

    Optional<WarLeague> findNthWarLeague(int n);

    void persistAndFlush(WarLeague warLeague);
}
