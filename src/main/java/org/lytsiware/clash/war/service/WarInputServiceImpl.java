package org.lytsiware.clash.war.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Application;
import org.lytsiware.clash.core.domain.LockService;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.domain.player.PlayerInOut;
import org.lytsiware.clash.core.domain.player.PlayerRepository;
import org.lytsiware.clash.core.service.integration.SiteIntegrationService;
import org.lytsiware.clash.donation.service.PlayerCheckInService;
import org.lytsiware.clash.war.domain.aggregation.PlayerAggregationWarStatsRepository;
import org.lytsiware.clash.war.domain.league.WarLeague;
import org.lytsiware.clash.war.domain.league.WarLeagueRepository;
import org.lytsiware.clash.war.domain.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStatsRepository;
import org.lytsiware.clash.war.domain.playerwarstat.WarPhaseStats;
import org.lytsiware.clash.war.dto.input.WarStatsInputDto;
import org.lytsiware.clash.war.service.integration.statsroyale.StatsRoyaleDateParse;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityNotFoundException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class WarInputServiceImpl implements WarInputService {
    private final SiteIntegrationService<List<WarStatsInputDto>> siteIntegrationService;

    private final StatsRoyaleDateParse statsRoyaleDateParse;

    private final PlayerWarStatsService playerWarStatsService;

    private final WarLeagueRepository warLeagueRepository;

    private final PlayerWarStatsRepository playerWarStatsRepository;

    private final WarLeagueService warLeagueService;

    private final PlayerAggregationWarStatsService playerAggregationWarStatsService;

    private final LockService lockService;

    private final PlayerCheckInService playerCheckInService;

    private final PlayerRepository playerRepository;

    private final PlayerAggregationWarStatsRepository playerAggregationWarStatsRepository;


    @Override
    public List<WarStatsInputDto> getWarStatsFromSite() {
        return siteIntegrationService.retrieveData();
    }

    @Override
    @Transactional
    public void recalculateWarStatsNoAffected(LocalDate leagueDate, int leagueSpan) throws EntityExistsException {

        try {
            lock();
            WarLeague warLeague = warLeagueRepository.findByStartDate(leagueDate).orElseThrow(() ->
                    new EntityNotFoundException("League with start date " + leagueDate + " not found"));
            doRecalculate(warLeague, leagueSpan);
        } catch (Exception ex) {
            log.error("Exception during war stats recalculation", ex);
        } finally {
            unlock();
        }
    }

    private void doRecalculate(WarLeague warLeague, int leagueSpan) {
        playerAggregationWarStatsService.deleteInBatch(warLeague.getStartDate(), WarConstants.leagueSpan);
        playerWarStatsRepository.flush();
        warLeagueService.calculateLeagueAvgsAndSave(warLeague);
        playerAggregationWarStatsService.calculateAndUpdateStats(warLeague.getStartDate(), leagueSpan);
    }


    @Override
    @Transactional
    public void recalculateWarStatsIncludingAffected(LocalDate leagueDate, int leagueSpan) throws EntityExistsException {
        try {
            lock();

            List<WarLeague> affectedLeagues = warLeagueRepository.findFirstNthWarLeaguesAfterDate(leagueDate, WarConstants.leagueSpan).stream()
                    .distinct()
                    .collect(Collectors.toList());

            if (affectedLeagues.isEmpty() || !affectedLeagues.get(0).getStartDate().equals(leagueDate)) {
                throw new EntityNotFoundException("League with start date " + leagueDate + " not found");
            }

            // warleague is recorded even when the war is not yet finished. For these leagues we don' t want
            // to calculate aggregation stats. These leagues are distinguished by the fact that they don' t have aggregation stats at all.
            for (WarLeague affectedLeague : affectedLeagues) {
                if (!affectedLeagues.get(0).getStartDate().equals(affectedLeague.getStartDate()) &&
                        playerAggregationWarStatsRepository.findByDateAndLeagueSpan(affectedLeague.getStartDate(), WarConstants.leagueSpan).isEmpty()) {
                    break;
                }

                this.doRecalculate(affectedLeague, WarConstants.leagueSpan);
            }

        } catch (Exception ex) {
            log.error("Exception during war stats recalculation", ex);
        } finally {
            unlock();
        }
    }


    @Override
    @Async(Application.Config.WAR_INPUT_EXECUTOR)
    @Transactional
    public CompletableFuture<String> saveWarStatsAndUpdateStatistics(List<PlayerWarStat> statsList) {
        try {
            lock();
            WarLeague warLeague = statsList.get(0).getWarLeague();
            WarLeague warLeagueDb = warLeagueRepository.findByStartDateEager(warLeague.getStartDate()).orElse(null);

            /*
             * A player may not exist in the DB if he joined and left the clan before the scheduler that persists the players had run.
             * Still he may participated in the war. We need to checkin the new player just before the start date of the warLeague and
             * checkout the player 12 hours later.
             */
            Map<String, Player> players = checkInCheckoutGonePlayers(statsList, warLeague);

            if (warLeagueDb != null) {
                clearPreviousLeagueStats(warLeagueDb);
                //performance optimization, add the already attached to entity managed players on the playerWarStat otherwise for
                // each stat will try to load the players from the db resulting to n queries (n the size of playerWarStat)
                for (PlayerWarStat pws : warLeague.getPlayerWarStats()) {
                    pws.setPlayer(players.get(pws.getPlayer().getTag()));
                }
            }

            warLeagueService.calculateLeagueAvgsAndSave(warLeague);
            playerWarStatsRepository.saveAll(statsList);
            playerWarStatsRepository.flush();

            playerAggregationWarStatsService.calculateAndUpdateStats(warLeague.getStartDate(), WarConstants.leagueSpan);

            return CompletableFuture.completedFuture("");

        } catch (Exception ex) {
            log.error("Error in war stats calculations", ex);
            return CompletableFuture.completedFuture(ex.getMessage());
        } finally {
            unlock();
        }
    }

    /*
     * Checks in and checks out players that are participating in the war but they are not in the players tables
     * (this may occur in case they joined clan, participated the war and left the clan before the scheduler that persists
     * the players have run)
     *
     * @return all the recorded players
     */
    private Map<String, Player> checkInCheckoutGonePlayers(List<PlayerWarStat> statsList, WarLeague warLeague) {

        Map<String, Player> playersDb = playerRepository.loadAll();
        List<Player> transientPlayers = statsList.stream().map(PlayerWarStat::getPlayer).filter(player -> !playersDb.containsKey(player.getTag())).
                peek(player -> log.debug("TRANSIENT PLAYER {}", player)).collect(Collectors.toList());

        for (Player transientPlayer : transientPlayers) {
            Player attachedPlayer = playerCheckInService.checkinPlayer(transientPlayer, warLeague.getStartDate().atTime(warLeague.getTime().minusHours(1)));
            playerCheckInService.checkoutPlayer(transientPlayer.getTag(), warLeague.getStartDate().atTime(warLeague.getTime().plusHours(12)));
            playersDb.put(attachedPlayer.getTag(), attachedPlayer);
        }
        return playersDb;
    }


    private void clearPreviousLeagueStats(WarLeague warLeagueDb) {
        playerWarStatsRepository.deleteInBatch(warLeagueDb.getPlayerWarStats());
        playerAggregationWarStatsService.deleteInBatch(warLeagueDb.getStartDate(), WarConstants.leagueSpan);
        warLeagueRepository.delete(warLeagueDb);
        playerWarStatsRepository.flush();
    }

    private void deleteWarAndAggrStats(List<PlayerWarStat> statsList, WarLeague warLeagueDb) {
        playerWarStatsRepository.deleteInBatch(warLeagueDb.getPlayerWarStats());
        playerAggregationWarStatsService.deleteInBatch(warLeagueDb.getStartDate(), WarConstants.leagueSpan);
        warLeagueDb.getPlayerWarStats().clear();
        playerWarStatsRepository.flush();
        statsList.forEach(playerWarStat -> playerWarStat.setWarLeague(warLeagueDb));
        playerWarStatsRepository.saveAll(statsList);
        playerWarStatsRepository.flush();
    }


    @Override
    public List<WarStatsInputDto> getPlayerWarStatsForInput(boolean includeNotParticipating) {
        List<WarStatsInputDto> siteAllWarLeagueStats = getWarStatsFromSite();
        for (WarStatsInputDto siteWarLeagueStat : siteAllWarLeagueStats) {

            LocalDateTime leagueStartDate = statsRoyaleDateParse.parseDescriptiveDate(siteWarLeagueStat.getLeagueName(), LocalDateTime.now())
                    .minusDays(WarConstants.WAR_DURATION);

            Map<Player, PlayerInOut> playerNotParticipated = (includeNotParticipating ? playerWarStatsService.findPlayersNotParticipatedInWar(siteWarLeagueStat, leagueStartDate, 240) : new HashMap<>());

            List<WarStatsInputDto.PlayerWarStatInputDto> playerNotParticipatedWarStatsInputDto = updateDeleteStatusFonNotParticipated(playerNotParticipated, leagueStartDate, 60 * 36);

            siteWarLeagueStat.setPlayersNotParticipated(playerNotParticipatedWarStatsInputDto);

            siteWarLeagueStat.setStartDate(leagueStartDate);

        }

        normalizeWarInputData(siteAllWarLeagueStats);

        return siteAllWarLeagueStats;
    }

    @Override
    @Transactional
    public void addNotParticipated(LocalDate leagueDate, int leagueSpan) {
        WarLeague warLeague = warLeagueRepository.findByStartDateEager(leagueDate).get();

        Set<String> playersInWarSet = warLeague.getPlayerWarStats().stream()
                .map(pws -> pws.getPlayer().getTag()).collect(Collectors.toSet());


        LocalDateTime startDate = warLeague.getStartDate().atTime(warLeague.getTime());
        Set<PlayerInOut> checkedInAtDate = new HashSet<>(playerCheckInService.findCheckedInPlayersAtDate(startDate));
        Set<PlayerInOut> checkedInWithFaultTollerance = new HashSet<>(playerCheckInService.findCheckedInPlayersAtDate(startDate.plusMinutes(240)));
        checkedInAtDate.addAll(checkedInWithFaultTollerance);

        Map<String, PlayerInOut> allCheckedInPlayers = checkedInAtDate.stream().collect(Collectors.toMap(PlayerInOut::getTag, Function.identity()));

        List<String> checkedInPlayersTagsNotParticipated = checkedInAtDate.stream()
                .map(PlayerInOut::getTag).filter(tag -> !playersInWarSet.contains(tag)).collect(Collectors.toList());

        //better load all and filter them out than hit n times the db for each one
        Map<String, Player> allPlayers = playerRepository.loadAll();

        Map<Player, PlayerInOut> playersNotParticipated = checkedInPlayersTagsNotParticipated.stream().collect(Collectors.toMap(allPlayers::get, allCheckedInPlayers::get));
        for (Player player : playersNotParticipated.keySet()) {
            PlayerWarStat playerWarStat = PlayerWarStat.builder()
                    .warPhaseStats(WarPhaseStats.builder()
                            .gamesWon(0)
                            .gamesGranted(0)
                            .gamesLost(0)
                            .build())
                    .collectionPhaseStats(CollectionPhaseStats.builder()
                            .cardsWon(0)
                            .gamesPlayed(0)
                            .build())
                    .player(player)
                    .warEligible(true)
                    .build();
            playerWarStat.setWarLeague(warLeague);
            playerWarStatsRepository.save(playerWarStat);

        }
    }

    private List<WarStatsInputDto.PlayerWarStatInputDto> updateDeleteStatusFonNotParticipated(Map<Player, PlayerInOut> playerNotParticipated, LocalDateTime startDate, int faultTolerance) {

        List<WarStatsInputDto.PlayerWarStatInputDto> notParticipatedInputDtoList = new ArrayList<>();

        for (Player player : playerNotParticipated.keySet()) {
            WarStatsInputDto.PlayerWarStatInputDto playerWarStatsInputDto = WarStatsInputDto.PlayerWarStatInputDto
                    .zeroFieldPlayerWarStatInputDto(player.getTag(), player.getName());
            PlayerInOut pIO = playerNotParticipated.get(player);
            if (pIO.getCheckIn().isAfter(startDate.minusMinutes(faultTolerance))) {
                playerWarStatsInputDto.setDelete(true);
            }
            notParticipatedInputDtoList.add(playerWarStatsInputDto);
        }

        return notParticipatedInputDtoList;
    }

    /**
     * Normalizes the data as following
     * <ul><li>Sorts the data per games won</li>
     * <li> updates the collection games played from the db stats</li>
     * <li> if a player has earned collection day cards but has no wins or looses add +1 to games not played</li>
     * <li> if the stats from the site shows that the player has more collection cards won than the ones stored in the db (if any) add +1 to collection battles played</li>
     * </ul>
     *
     * @param siteAllWarLeagueStats
     */
    private void normalizeWarInputData(List<WarStatsInputDto> siteAllWarLeagueStats) {
        for (WarStatsInputDto siteWarLeagueStat : siteAllWarLeagueStats) {
            siteWarLeagueStat.getPlayerWarStats().stream().filter(player -> player.getGamesGranted() == 0).forEach(
                    player -> {
                        player.setGamesGranted(1);
                        player.setGamesNotPlayed(1);
                    }
            );
            siteWarLeagueStat.getPlayerWarStats().sort(Comparator.comparing(WarStatsInputDto.PlayerWarStatInputDto::getGamesWon)
                    .thenComparing(WarStatsInputDto.PlayerWarStatInputDto::getGamesLost)
                    .thenComparing(WarStatsInputDto.PlayerWarStatInputDto::getGamesNotPlayed)
                    .thenComparing(WarStatsInputDto.PlayerWarStatInputDto::getCards).reversed());

            Set<PlayerWarStat> playerWarStats = warLeagueRepository.findByStartDate(siteWarLeagueStat.getStartDate().toLocalDate())
                    .map(WarLeague::getPlayerWarStats).orElse(new HashSet<>());
            for (PlayerWarStat pwsDb : playerWarStats) {
                siteWarLeagueStat.getPlayerWarStats().stream()
                        // find player
                        .filter(playerWarStatInputDto -> playerWarStatInputDto.getTag().equals(pwsDb.getPlayer().getTag()))
                        .forEach(playerWarStatInputDto -> {
                            playerWarStatInputDto.setCollectionBattlesPlayed(pwsDb.getCollectionPhaseStats().getGamesPlayed());
                            // check the ones that the input shows that they have won more cards that was stored in the db
                            // add one more collection battle (there is no way they could have more cards if they did not player another battle)
                            if (playerWarStatInputDto.getCards() > pwsDb.getCollectionPhaseStats().getCardsWon()) {
                                playerWarStatInputDto.setCollectionBattlesPlayed(pwsDb.getCollectionPhaseStats().getGamesPlayed() + 1);
                            } else {
                                playerWarStatInputDto.setCollectionBattlesPlayed(pwsDb.getCollectionPhaseStats().getGamesPlayed());
                            }
                        });

            }
        }
    }

    private void unlock() {
        lockService.unlock();
    }

    private void lock() {
        if (!lockService.lock()) {
            throw new RuntimeException("Could not lock");
        }
    }
}
