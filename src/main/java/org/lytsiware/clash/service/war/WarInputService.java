package org.lytsiware.clash.service.war;

import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;

import javax.persistence.EntityExistsException;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface WarInputService {

    List<WarStatsInputDto> getWarStatsFromSite();

    /**
     * Recalculates the aggregation stats and league avg stats
     *
     * @param leagueDate
     * @param leagueSpan
     * @throws EntityExistsException if there is no league at that date
     */
    void recalculateWarStatsNoAffected(LocalDate leagueDate, int leagueSpan) throws EntityExistsException;

    /**
     * Recalculates the aggregation stats and leagues avg stats of the leagues starting from the start date (inclusive)
     * and after that date
     *
     * @param leagueDate
     * @param leagueSpan
     * @throws EntityExistsException if there is no league at that date
     */
    @Transactional
    void recalculateWarStatsIncludingAffected(LocalDate leagueDate, int leagueSpan) throws EntityExistsException;

    /**
     * 1. Saves the provided war stats and the league, 2. calculates and saves the aggregation war stats
     */
    CompletableFuture<String> saveWarStatsAndUpdateStatistics(List<PlayerWarStat> statsList) throws EntityExistsException;

    /**
     * Gets the warstats from the integration sites and corrects them if possible
     *
     * @param includeNotParticipating if true also includes the not-participating players
     */
    List<WarStatsInputDto> getPlayerWarStatsForInput(boolean includeNotParticipating);

    /**
     * Adds warstats for the players that have not participated in the war.
     * This method should be called exceptionally as the non-participated players are normally
     * persisted {@link WarInputService#getPlayerWarStatsForInput}
     */
    void addNotParticipated(LocalDate leagueDate, int leagueSpan);
}
