package org.lytsiware.clash.donation.service;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.donation.domain.PlayerWeeklyStats;

import java.util.List;

public interface UpdateStatService {

    /**
     * marks the players not in clan
     *
     * @param playerWeeklyStats the players currently in the clan
     */
    void markPlayerIsInClan(List<PlayerWeeklyStats> playerWeeklyStats);

    /**
     * Updates the card donation and chest contribution values
     * @param stats the stats containing the players to be updated and thei new value
     * @param week the week the update should happen
     * @param updateBiggerOnly if true then each value is only updated if the new value is bigger than the current one
     */
    void updatePlayerWeeklyStats(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly);

    /**
     * Updates the chest contributions
     * @param stats the stats containing the players to be updated and their new chest contribution
     * @param week the week the update should  happen
     * @param updateBiggerOnly if true the contributions are only updated if the new contribution value is bigger than the current.
     * if the current contribution is null then it is only updated if the new one is bigger than 0. A null current contribution, indicates
     * that the player has joined after the chan chest was started
     * @deprecated there is no CC anymore
     */
    @Deprecated
    void updateChestContibutionAndRole(List<PlayerWeeklyStats> stats, Week week, boolean updateBiggerOnly);

}
