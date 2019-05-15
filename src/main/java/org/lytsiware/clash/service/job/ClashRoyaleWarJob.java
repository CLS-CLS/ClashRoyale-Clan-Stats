package org.lytsiware.clash.service.job;


import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.service.integration.clashapi.ClashRoyaleRestIntegrationService;
import org.lytsiware.clash.service.war.PlayerWarStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;

@Service
public class ClashRoyaleWarJob {

    @Autowired
    ClashRoyaleRestIntegrationService clashRoyaleRestIntegrationService;

    @Autowired
    WarLeagueRepository warLeagueRepository;

    @Autowired
    PlayerWarStatsRepository playerWarStatsRepository;

    @Autowired
    PlayerWarStatsService playerWarStatsService;

    public void clashRoyaleJob() {
        WarLeague warLeague = clashRoyaleRestIntegrationService.getWarLeagueStatsForCurrentWar();
        Optional<WarLeague> warLeagueDb = warLeagueRepository.findByStartDate(warLeague.getStartDate());
        if (warLeagueDb.isPresent()) {
            updateData(warLeagueDb.get(), warLeague);
        } else {
            insertData(warLeague);
        }
    }

    private void updateData(WarLeague warLeagueDb, WarLeague warLeague) {
        for (PlayerWarStat pws : warLeague.getPlayerWarStats()) {
            updateCollectionsPlayed(warLeagueDb.getPlayerWarStats(), pws, warLeagueDb);
        }
        playerWarStatsRepository.saveAll(warLeagueDb.getPlayerWarStats());
    }

    private void updateCollectionsPlayed(Set<PlayerWarStat> playerWarStats, PlayerWarStat pws, WarLeague warLeagueDb) {
        Optional<PlayerWarStat> pwsDb = playerWarStats.stream()
                .filter(playerWarStat -> playerWarStat.getPlayer().getTag().equals(pws.getPlayer().getTag()))
                .findFirst();
        if (pwsDb.isPresent()) {
            pwsDb.get().getCollectionPhaseStats().setGamesPlayed(pws.getCollectionPhaseStats().getGamesPlayed());
            pwsDb.get().getCollectionPhaseStats().setCardsWon(pws.getCollectionPhaseStats().getCardsWon());
        } else {
            pws.setWarLeague(warLeagueDb);
            playerWarStatsRepository.save(pws);
        }

    }

    private void insertData(WarLeague warLeague) {
        warLeagueRepository.save(warLeague);
        playerWarStatsRepository.saveAll(warLeague.getPlayerWarStats());
    }
}
