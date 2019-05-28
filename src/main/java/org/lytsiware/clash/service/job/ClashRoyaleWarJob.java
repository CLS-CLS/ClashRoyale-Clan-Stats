package org.lytsiware.clash.service.job;


import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.league.WarLeagueRepository;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.service.integration.clashapi.ClashRoyaleRestIntegrationService;
import org.lytsiware.clash.service.integration.clashapi.CurrentWarDto;
import org.lytsiware.clash.service.job.scheduledname.AbstractSelfScheduledJob;
import org.lytsiware.clash.service.job.scheduledname.ScheduledName;
import org.lytsiware.clash.service.war.PlayerWarStatsService;
import org.lytsiware.clash.utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class ClashRoyaleWarJob extends AbstractSelfScheduledJob {

    @Autowired
    ClashRoyaleRestIntegrationService clashRoyaleRestIntegrationService;

    @Autowired
    WarLeagueRepository warLeagueRepository;

    @Autowired
    PlayerWarStatsRepository playerWarStatsRepository;

    @Autowired
    PlayerWarStatsService playerWarStatsService;

    @Autowired
    PlayerRepository playerRepository;

    @Scheduled(fixedRate = 1000 * 60 * 60 * 24)
    @ScheduledName("ClashRoyaleWarJob")
    public void runPeriodically() {
        log.info("Clash Royale War Job Triggered by Fixed Scheduler");
        fixedScheduler();
    }


    /**
     * inserts data for the already recorded players. Players not yet recorded will be omitted as by the time
     * the method is called again the scheduler the method responsible to record the players will have already run and the new
     * players will now be in the database.
     */
    @Override
    @Retryable(backoff = @Backoff(delay = 1000 * 60 * 60))
    public Date run() {
        log.info("Run ClashRoyaleJob");
        CurrentWarDto dto = clashRoyaleRestIntegrationService.getDataFromSite();
        WarLeague warLeague = clashRoyaleRestIntegrationService.createWarLeagueFromData(dto);
        Optional<WarLeague> warLeagueDb = warLeagueRepository.findByStartDate(warLeague.getStartDate());
        Map<String, Player> recordedPlayers = playerRepository.loadAll();
        if (warLeagueDb.isPresent()) {
            updateData(warLeagueDb.get(), warLeague);
        } else {
            insertData(warLeague);
        }
        Date date = null;
        if (dto.getState() == CurrentWarDto.State.COLLECTION_DAY) {
            date = Utils.convertToDate(dto.getEndDate().plusMinutes(30));
        } else if (dto.getState() == CurrentWarDto.State.WAR_DAY) {
            date = Utils.convertToDate(dto.getEndDate().plusDays(1));
        } else {
            date = new Date();
        }
        log.info("Next Self Scheduler Execution at {}", date);
        return date;

    }

    private void updateData(WarLeague warLeagueDb, WarLeague warLeague) {

        for (PlayerWarStat pwsDb : warLeagueDb.getPlayerWarStats()) {
            Optional<PlayerWarStat> sitePlayerWarStat = warLeague.getPlayerWarStats().stream()
                    .filter(pws -> pws.getPlayer().getTag().equals(pwsDb.getPlayer().getTag())).findFirst();
            if (sitePlayerWarStat.isPresent()) {
                pwsDb.getCollectionPhaseStats().setCardsWon(sitePlayerWarStat.get().getCollectionPhaseStats().getCardsWon());
                pwsDb.getCollectionPhaseStats().setGamesPlayed(sitePlayerWarStat.get().getCollectionPhaseStats().getGamesPlayed());
            }
        }
        playerWarStatsRepository.saveAll(warLeagueDb.getPlayerWarStats());
    }


    /**
     * inserts data for the already recorded players. Players not yet recorded will be omitted. This is OJ as by the time
     * the method is called again the scheduler who is responsible to record the players will have already run and the new
     * players will now be in the database.
     */
    private void insertData(WarLeague warLeague) {
        Map<String, Player> recordedPlayers = playerRepository.loadAll();

        warLeagueRepository.save(warLeague);
        List<PlayerWarStat> playerWarStatsToRecord = warLeague.getPlayerWarStats().stream()
                .filter(playerWarStat -> {
                    boolean exists = recordedPlayers.containsKey(playerWarStat.getPlayer().getTag());
                    if (!exists) {
                        log.info("Stats for Player {} are omitted as he is not yet recorded ", playerWarStat.getPlayer());
                    }
                    return exists;
                })
                .collect(Collectors.toList());
        playerWarStatsRepository.saveAll(playerWarStatsToRecord);
    }


}
