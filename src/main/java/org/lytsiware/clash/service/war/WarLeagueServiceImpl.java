package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class WarLeagueServiceImpl implements WarLeagueService {
    @Autowired
    WarLeagueRepository warLeagueRepository;

    @Override
    public WarLeague createLeague(String name, LocalDate startDate) {
        return null;
    }

    @Override
    public WarLeague findLeagueForDate(LocalDate date, WarLeaguePhase warLeaguePhase) {
        return null;
    }

    @Override
    public List<WarLeague> getAffectedLeagues(LocalDate date, int span) {
        return warLeagueRepository.findLeaguesAroundDate(date, span);
    }
}
