package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerInOut;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.PlaywerWarStatsWithAvgsDto;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface PlayerWarStatsService {

    PlaywerWarStatsWithAvgsDto getLatestPlayerWarStatsUntil(String tag, LocalDate untilDate);

    CompletableFuture<String> saveWarStatsAndUpdateStatistics(List<PlayerWarStat> statsList);

    /**
     * Finds the players that could have participated in the war but they did not.
     * Players that could participated in war are the one that was in the clan the time the war had started
     * (there were checked in before the war has started and also was not checked out or they checkout out after the war had started)
     * Because the checkin checkout dates are not precise (they depend on the scheduler run time) there is a optional fault tolerance in minutes.
     * A player can be considered checked in when   checkinTime  - faultTolleranceInMinutes < leagueStartDate
     *
     * @param playersInWar
     * @param date
     * @param faultTolleranceInMinutes
     * @return
     */
    Map<Player, PlayerInOut> findPlayersNotParticipatedInWar(WarStatsInputDto playersInWar, LocalDateTime date, Integer faultTolleranceInMinutes);

    List<PlayerWarStat> findWarStatsForWar(Integer deltaWar);
}
