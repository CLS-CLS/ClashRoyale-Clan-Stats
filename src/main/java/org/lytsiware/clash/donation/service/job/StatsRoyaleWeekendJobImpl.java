package org.lytsiware.clash.donation.service.job;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.service.integration.SiteIntegrationService;
import org.lytsiware.clash.core.service.integration.SiteQualifier;
import org.lytsiware.clash.core.service.job.scheduledname.ScheduledName;
import org.lytsiware.clash.donation.domain.PlayerWeeklyStats;
import org.lytsiware.clash.donation.service.PlayerCheckInService;
import org.lytsiware.clash.donation.service.UpdateStatsServiceImpl;
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
import java.util.stream.Collectors;

@Service
@Profile("statsRoyale")
@Transactional(Transactional.TxType.REQUIRED)
public class StatsRoyaleWeekendJobImpl {

    private final PlayerCheckInService playerInOutService;
    private final Logger logger = LoggerFactory.getLogger(StatsRoyaleWeekendJobImpl.class);

    private final SiteIntegrationService<List<PlayerWeeklyStats>> statsRoyaleSiteService;

    private final SiteIntegrationService<List<PlayerWeeklyStats>> deckShopSiteService;

    private final UpdateStatsServiceImpl updateStatsService;

    @Autowired
    public StatsRoyaleWeekendJobImpl(@SiteQualifier(SiteQualifier.Name.STATS_ROYALE) SiteIntegrationService statsRoyaleSiteService,
                                     @SiteQualifier(SiteQualifier.Name.DECK_SHOP) SiteIntegrationService deckShopSiteService,
                                     UpdateStatsServiceImpl updateStatsService, PlayerCheckInService playerInOutServiceImpl) {
        this.statsRoyaleSiteService = statsRoyaleSiteService;
        this.deckShopSiteService = deckShopSiteService;
        this.updateStatsService = updateStatsService;
        this.playerInOutService = playerInOutServiceImpl;
    }


    @Scheduled(cron = "${cron.midweek}")
    @ScheduledName(name = "midweekRunner")
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
            List<Player> currentPlayers = stats.stream().map(PlayerWeeklyStats::getPlayer).collect(Collectors.toList());
            playerInOutService.markPlayersInClan(currentPlayers);
        } catch (Exception e) {
            logger.error("oops", e);
            throw e;
        }
    }

    @Scheduled(cron = "${cron.sundayRunner}")
    @ScheduledName(name = "sundayRunner")
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
