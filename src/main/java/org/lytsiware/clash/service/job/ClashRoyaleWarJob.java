package org.lytsiware.clash.service.job;


import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.service.integration.clashapi.ClashRoyaleRestIntegrationService;
import org.lytsiware.clash.service.war.PlayerWarStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ClashRoyaleWarJob {

    @Autowired
    ClashRoyaleRestIntegrationService clashRoyaleRestIntegrationService;

    @Autowired
    PlayerWarStatsService playerWarStatsService;

    public void clashRoyaleJob() {
        WarLeague warLeague = clashRoyaleRestIntegrationService.getWarLeagueStatsForCurrentWar();
        playerWarStatsService.mergePlayerWarStats()

    }
}
