package org.lytsiware.clash.service.job;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.clan.PlayerInOutServiceImpl;
import org.lytsiware.clash.service.clan.UpdateStatsServiceImpl;
import org.lytsiware.clash.service.integration.SiteIntegrationService;
import org.lytsiware.clash.service.integration.SiteQualifier;
import org.lytsiware.clash.service.job.scheduledname.ScheduledName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("statsRoyale")
@Transactional(Transactional.TxType.REQUIRED)
public class StatsRoyaleWeekendJobImpl {

    private final PlayerInOutServiceImpl playerInOutServiceImpl;
    private Logger logger = LoggerFactory.getLogger(StatsRoyaleWeekendJobImpl.class);

    private SiteIntegrationService<List<PlayerWeeklyStats>> statsRoyaleSiteService;

    private SiteIntegrationService<List<PlayerWeeklyStats>> deckShopSiteService;

    private UpdateStatsServiceImpl updateStatsService;

    @Autowired
    public StatsRoyaleWeekendJobImpl(@SiteQualifier(SiteQualifier.Name.STATS_ROYALE) SiteIntegrationService statsRoyaleSiteService,
                                     @SiteQualifier(SiteQualifier.Name.DECK_SHOP) SiteIntegrationService deckShopSiteService,
                                     UpdateStatsServiceImpl updateStatsService, PlayerInOutServiceImpl playerInOutServiceImpl) {
        this.statsRoyaleSiteService = statsRoyaleSiteService;
        this.deckShopSiteService = deckShopSiteService;
        this.updateStatsService = updateStatsService;
        this.playerInOutServiceImpl = playerInOutServiceImpl;
    }


    @Scheduled(cron = "${cron.midweek}")
    @ScheduledName("midweekRunner")
    public void midweek() {
        try {
            logger.info("Job Triggered at {}", LocalDateTime.now());

            List<PlayerWeeklyStats> stats = statsRoyaleSiteService.retrieveData();
            try {
                List<PlayerWeeklyStats> deckproStats = deckShopSiteService.retrieveData();
                updateReceivedCards(stats, deckproStats);
            } catch (Exception ex) {
                logger.error("Exception while retrieving stats from deckshop", ex);
            }
            updateStatsService.updatePlayerWeeklyStats(stats, Week.now(), true);
            playerInOutServiceImpl.markPlayersInClan(stats);
        } catch (Exception e) {
            logger.error("oops", e);
            throw e;
        }
    }

    @Scheduled(cron = "${cron.sundayRunner}")
    @ScheduledName("sundayRunner")
    @Retryable(maxAttempts = 2)
    public void sundayRunner() {
        logger.info("Sunday Runner triggered");
        midweek();
    }


    private void updateReceivedCards(List<PlayerWeeklyStats> stats, List<PlayerWeeklyStats> deckproStats) {
        for (PlayerWeeklyStats pws : stats) {
            PlayerWeeklyStats deckProStat = deckproStats.stream().filter(dps -> dps.getPlayer().getTag().equals(pws.getPlayer().getTag())).findAny().orElse(null);
            if (deckProStat != null) {
                pws.setCardsReceived(deckProStat.getCardsReceived());
            }
        }
    }

}
