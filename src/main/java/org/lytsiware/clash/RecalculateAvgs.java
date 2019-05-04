package org.lytsiware.clash;

import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.service.war.WarLeagueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class RecalculateAvgs implements CommandLineRunner {

    @Autowired
    WarLeagueService warLeagueService;

    @Autowired
    WarLeagueRepository warLeagueRepository;


    @Override
    public void run(String... args) {
        List<WarLeague> leagues = warLeagueRepository.findAll();

        for (WarLeague warLeague : leagues) {
            warLeagueService.calculateLeagueAvgs(warLeague);
        }

        warLeagueRepository.saveAll(leagues);

    }
}
