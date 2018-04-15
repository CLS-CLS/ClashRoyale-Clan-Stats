package org.lytsiware.clash.service.job;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.clan.UpdateStatsServiceImpl;
import org.lytsiware.clash.service.integration.SiteIntegrationService;
import org.lytsiware.clash.service.integration.SiteQualifier;
import org.lytsiware.clash.service.job.scheduledname.ScheduledName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("statsRoyale")
@Transactional(Transactional.TxType.REQUIRED)
public class StatsRoyaleWeekendJobImpl {

    private Logger logger = LoggerFactory.getLogger(StatsRoyaleWeekendJobImpl.class);

    private ClanStatsService clanStatsService;

    private SiteIntegrationService statsRoyaleSiteService;

    private SiteIntegrationService deckShopSiteService;

    private UpdateStatsServiceImpl updateStatsService;

    @Autowired
    public StatsRoyaleWeekendJobImpl(ClanStatsService clanStatsService,
                                     @SiteQualifier(SiteQualifier.Name.STATS_ROYALE) SiteIntegrationService statsRoyaleSiteService,
                                     @SiteQualifier(SiteQualifier.Name.DECK_SHOP) SiteIntegrationService deckShopSiteService,
                                     UpdateStatsServiceImpl updateStatsService) {
        this.clanStatsService = clanStatsService;
        this.statsRoyaleSiteService = statsRoyaleSiteService;
        this.deckShopSiteService = deckShopSiteService;
        this.updateStatsService = updateStatsService;
    }

    /**
     * Important !! Make sure the first run is after the clan chest has started or else the previous chest's score will
     * be persisted and it will only be updated if the new score is bigger than the previous' week score!
     */
    @Scheduled(cron = "${cron.weekend}", zone = ZoneIdConfiguration.zoneId)
    @ScheduledName("weekendRunner")
    public void run() {
        try {
            logger.info("Job Triggered at {}", LocalDateTime.now());

            List<PlayerWeeklyStats> stats = statsRoyaleSiteService.retrieveData();
            try {
                List<PlayerWeeklyStats> deckproStats = deckShopSiteService.retrieveData();
                updateReceivedCards(stats, deckproStats);
            } catch (Exception ex) {
                logger.error("Exception while retrieving stats from deckshop", ex);
            }

            updateStatsService.updateOrInsertDonationAndContributions(stats, Week.now(), true);
        } catch (Exception e) {
            logger.error("oops", e);
            throw e;
        }
    }

    @Scheduled(cron = "${cron.midweek}", zone = ZoneIdConfiguration.zoneId)
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
            //chest contribution taken reflects the previous week's cc not this one (this one has not started yet)
            stats.forEach(s -> s.setChestContribution(null));

            updateStatsService.updateOrInsertDonationAndContributions(stats, Week.now(), false);
        } catch (Exception e) {
            logger.error("oops", e);
            throw e;
        }

    }

    @ScheduledName("weekendRunner-final")
    @Scheduled(cron = "${cron.weekend.final}", zone = ZoneIdConfiguration.zoneId)
    public void extraCheck() {
        run();
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
