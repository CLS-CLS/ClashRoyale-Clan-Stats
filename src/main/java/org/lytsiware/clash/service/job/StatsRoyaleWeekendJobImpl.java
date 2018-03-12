package org.lytsiware.clash.service.job;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.ZoneIdConfiguration;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.integration.StatsRoyaleSiteServiceImpl;
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

	@Autowired
    private ClanStatsService clanStatsService;

    @Autowired
    private StatsRoyaleSiteServiceImpl siteIntegrationService;

	/**
	 * Important !! Make sure the first run is after the clan chest has started or else the previous chest's score will
     * be persisted and it will only be updated if the new score is bigger than the previous' week score!
	 */

    @Scheduled(cron = "${cron.weekend}", zone = ZoneIdConfiguration.zoneId)
	@ScheduledName("weekendRunner")
	public void run() {
		try {
			logger.info("Job Triggered at {}", LocalDateTime.now());
			List<PlayerWeeklyStats> stats = siteIntegrationService.retrieveData(true);
			clanStatsService.updateOrInsertDonationAndContributions(stats, Week.now(), true);
		} catch (Exception e) {
			logger.error("oops", e);
			throw e;
		}
	}


    @Scheduled(cron = "${cron.weekend.final}", zone = ZoneIdConfiguration.zoneId)
	public void extraCheck(){
		run();
	}
	
}
