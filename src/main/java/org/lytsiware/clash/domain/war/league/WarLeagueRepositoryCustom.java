package org.lytsiware.clash.domain.war.league;

import java.time.LocalDate;
import java.util.List;

public interface WarLeagueRepositoryCustom {

    List<WarLeague> findLeaguesAroundDate(LocalDate date, int span);
}
