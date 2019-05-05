//package org.lytsiware.clash;
//
//import org.lytsiware.clash.domain.war.league.WarLeague;
//import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
//import org.lytsiware.clash.service.war.WarLeagueService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.stereotype.Service;
//
//import javax.transaction.Transactional;
//import java.time.LocalDate;
//import java.util.Comparator;
//import java.util.List;
//
//@Service
//@Transactional
//public class RecalculateAvgs implements CommandLineRunner {
//
//    @Autowired
//    WarLeagueService warLeagueService;
//
//    @Autowired
//    WarLeagueRepository warLeagueRepository;
//
//
//    @Override
//    public void run(String... args) {
//        List<WarLeague> leagues = warLeagueRepository.findAll();
//        leagues.sort(Comparator.comparing(WarLeague::getStartDate, LocalDate::compareTo));
//        leagues.get(0).setTotalTrophies(leagues.get(0).getTrophies());
//
//        for (int i = 1; i < leagues.size(); i++) {
//            leagues.get(i).setTotalTrophies(leagues.get(i - 1).getTotalTrophies() + leagues.get(i).getTrophies());
//        }
//
//        warLeagueRepository.saveAll(leagues);
//
//    }
//}
