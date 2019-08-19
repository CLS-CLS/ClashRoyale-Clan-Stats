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

    @Scheduled(fixedRate = 1000 * 60 * 60 * 12)
    @Retryable(backoff = @Backoff(delay = 1000 * 60 * 60), maxAttempts = 2)
    public void runPeriodically() {
        fixedScheduler();
    }


    /**
     * inserts data for the already recorded players. Players not yet recorded will be omitted as by the time
     * the method is called again the scheduler responsible to record the players will have already run and the new
     * players will now be in the database.
     */
    @Override
    public Date run() {
        log.info("Run ClashRoyaleJob");
        CurrentWarDto dto = clashRoyaleRestIntegrationService.getDataFromSite();
        WarLeague warLeague = clashRoyaleRestIntegrationService.createWarLeagueFromData(dto);
        Optional<WarLeague> warLeagueDb = warLeagueRepository.findByStartDate(warLeague.getStartDate());
        if (warLeagueDb.isPresent()) {
            updateData(warLeagueDb.get(), warLeague);
        } else {
            insertData(warLeague);
        }
        Date date;
        if (dto.getState() == CurrentWarDto.State.COLLECTION_DAY) {
            date = Utils.convertToDate(dto.getEndDate().plusMinutes(30));
        } else if (dto.getState() == CurrentWarDto.State.WAR_DAY) {
            date = Utils.convertToDate(dto.getEndDate().plusDays(2));
        } else {
            date = new Date();
        }
        log.info("Next Self Scheduler Execution at {}", date);
        return date;

    }

    private void updateData(WarLeague warLeagueDb, WarLeague warLeague) {
        Map<String, Player> recordedPlayers = playerRepository.loadAll();

        List<PlayerWarStat> playerWarStatsToRecord = warLeague.getPlayerWarStats().stream()
                .filter(playerWarStat -> {
                    boolean exists = recordedPlayers.containsKey(playerWarStat.getPlayer().getTag());
                    if (!exists) {
                        log.info("Stats for Player {} are omitted as he is not yet recorded ", playerWarStat.getPlayer());
                    }
                    return exists;
                })
                .collect(Collectors.toList());
        for (PlayerWarStat playerWarStat : playerWarStatsToRecord) {
            Optional<PlayerWarStat> playerWarStatDb = warLeagueDb.getPlayerWarStats().stream()
                    .filter(pwsDB -> pwsDB.getPlayer().getTag().equals(playerWarStat.getPlayer().getTag())).findFirst();
            if (playerWarStatDb.isPresent()) {
                playerWarStatDb.get().getCollectionPhaseStats().setCardsWon(playerWarStat.getCollectionPhaseStats().getCardsWon());
                playerWarStatDb.get().getCollectionPhaseStats().setGamesPlayed(playerWarStat.getCollectionPhaseStats().getGamesPlayed());
                playerWarStatsRepository.save(playerWarStatDb.get());
            } else {
                playerWarStat.setWarLeague(warLeagueDb);
                playerWarStatsRepository.save(playerWarStat);
            }
        }
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
