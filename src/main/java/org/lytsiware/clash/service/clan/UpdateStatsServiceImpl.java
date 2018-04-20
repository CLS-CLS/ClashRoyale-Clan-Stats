package org.lytsiware.clash.service.clan;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerRepository;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.service.UpdateStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UpdateStatsServiceImpl implements UpdateStatService {

    private Logger logger = LoggerFactory.getLogger(UpdateStatsServiceImpl.class);

    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;
    private PlayerRepository playerRepository;


    @Autowired
    public UpdateStatsServiceImpl(PlayerWeeklyStatsRepository playerWeeklyStatsRepository, PlayerRepository playerRepository) {
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.playerRepository = playerRepository;
    }


    @Override
    @Transactional(value = Transactional.TxType.REQUIRED)
    public void markPlayerIsInClan(List<PlayerWeeklyStats> playerWeeklyStats) {
        Map<String, Player> players = playerRepository.loadAll();
        Set<Player> playersInClan = playerWeeklyStats.stream().map(PlayerWeeklyStats::getPlayer).collect(Collectors.toSet());
        List<Player> playersToBeUpdated = new ArrayList<>();

        for (Player playerInClan : playersInClan) {
            Player player = players.remove(playerInClan.getTag());
            if (!player.getInClan()) {
                player.setInClan(true);
                playersToBeUpdated.add(player);
            }
        }
        //update players that are not in clan but have flag true
        for (Player playerNotInClan : players.values()) {
            if (playerNotInClan.getInClan()) {
                playerNotInClan.setInClan(false);
                playersToBeUpdated.add(playerNotInClan);
            }
        }

        playerRepository.saveOrUpdate(playersToBeUpdated);
    }


    @Override
    @Transactional(value = Transactional.TxType.REQUIRED)
    public void updateOrInsertDonationAndContributions(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly) {
        logger.info("Update/insert weekly stats for week {} , bigger-only={}", week.getWeek(), updateBiggerOnly);
        Map<String, PlayerWeeklyStats> dbStatsPerTag = playerWeeklyStatsRepository.findByWeek(week).stream().collect(Collectors.toMap(pws -> pws.getPlayer().getTag(), Function.identity()));

        List<PlayerWeeklyStats> toUpdate = new ArrayList<>();

        for (PlayerWeeklyStats newStat : stats) {
            PlayerWeeklyStats dbStat = dbStatsPerTag.get(newStat.getPlayer().getTag());
            if (dbStat != null) {
                if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getChestContribution(), dbStat.getChestContribution()) >= 1) {
                    logger.info("update contribution for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getChestContribution(),
                            newStat.getChestContribution());
                    dbStat.setChestContribution(newStat.getChestContribution());
                }
                if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getCardDonation(), dbStat.getCardDonation()) >= 1) {
                    logger.info("update donation for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getCardDonation(), newStat.getCardDonation());
                    dbStat.setCardDonation(newStat.getCardDonation());
                }
                if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getCardsReceived(), dbStat.getCardsReceived()) >= 1) {
                    logger.info("update requests for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getCardsReceived(), newStat.getCardsReceived());
                    dbStat.setCardsReceived(newStat.getCardsReceived());
                }
            } else {
                Player newPlayer = new Player(newStat.getPlayer().getTag(), newStat.getPlayer().getName(), newStat.getPlayer().getRole(), true);
                dbStat = PlayerWeeklyStats.builder()
                        .withPlayer(newPlayer)
                        .withWeek(week.getWeek())
                        .withChestContribution(newStat.getChestContribution())
                        .withCardDonation(newStat.getCardDonation())
                        .withCardsReceived(newStat.getCardsReceived()).build();
                dbStat.setWeek(week.getWeek());
                logger.info("add new player {} with chestContribution {} donation {} and requests {}", dbStat.getPlayer().getName(), dbStat.getChestContribution(),
                        dbStat.getCardDonation(), dbStat.getCardsReceived());
            }
            toUpdate.add(dbStat);
        }
        playerWeeklyStatsRepository.saveOrUpdateAll(toUpdate);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED)
    public void updateOrInsertNewDonationsAndRole(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly) {
        logger.info("Update/insert card donations for week {} , bigger-only={}", week.getWeek(), updateBiggerOnly);
        Map<String, PlayerWeeklyStats> dbStatsPerTag = playerWeeklyStatsRepository.findByWeek(week).stream().collect(Collectors.toMap(pws -> pws.getPlayer().getTag(), Function.identity()));

        List<PlayerWeeklyStats> toUpdate = new ArrayList<>();

        for (PlayerWeeklyStats newStat : stats) {
            PlayerWeeklyStats dbStat = dbStatsPerTag.get(newStat.getPlayer().getTag());
            if (dbStat != null) {
                dbStat.getPlayer().setRole(newStat.getPlayer().getRole());
                if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getCardDonation(), dbStat.getCardDonation()) >= 1) {
                    logger.info("update donation for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getCardDonation(), newStat.getCardDonation());
                    dbStat.setCardDonation(newStat.getCardDonation());
                }
            } else {
                Player newPlayer = new Player(newStat.getPlayer().getTag(), newStat.getPlayer().getName(), newStat.getPlayer().getRole(), true);
                dbStat = new PlayerWeeklyStats(newPlayer, week.getWeek(), null, newStat.getCardDonation(), 0, 0);
                dbStat.setWeek(week.getWeek());
                logger.info("add new player {} with donation {}", dbStat.getPlayer().getName(), dbStat.getCardDonation());
            }
            toUpdate.add(dbStat);
        }
        playerWeeklyStatsRepository.saveOrUpdateAll(toUpdate);
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED)
    public void updateChestContibutionAndRole(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly) {
        logger.info("Update chest contributions for week {} , bigger-only={}", week.getWeek(), updateBiggerOnly);
        Map<String, PlayerWeeklyStats> dbStatsPerTag = playerWeeklyStatsRepository.findByWeek(week).stream().collect(Collectors.toMap(pws -> pws.getPlayer().getTag(), Function.identity()));

        List<PlayerWeeklyStats> toUpdate = new ArrayList<>();

        for (PlayerWeeklyStats newStat : stats) {
            PlayerWeeklyStats dbStat = dbStatsPerTag.get(newStat.getPlayer().getTag());
            if (dbStat == null) {
                continue;
            }
            dbStat.getPlayer().setRole(newStat.getPlayer().getRole());
            if (!updateBiggerOnly || Comparator.nullsFirst(Integer::compare).compare(newStat.getChestContribution(), dbStat.getChestContribution()) >= 1) {
                logger.info("update contribution for player {} from {} to {}", dbStat.getPlayer().getName(), dbStat.getChestContribution(),
                        newStat.getChestContribution());
                dbStat.setChestContribution(newStat.getChestContribution());
            }
            toUpdate.add(dbStat);
        }
        playerWeeklyStatsRepository.saveOrUpdateAll(toUpdate);
    }

}
