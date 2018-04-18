package org.lytsiware.clash.dto;

import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;

public class NewPlayerOverallStats extends PlayerOverallStats {

    private boolean stillInClan;

    public NewPlayerOverallStats() {
        super();
    }

    public NewPlayerOverallStats(PlayerWeeklyStats playerWeeklyStats, boolean stillInClan) {
        super(playerWeeklyStats);
        this.stillInClan = stillInClan;
    }

    public boolean isStillInClan() {
        return stillInClan;
    }

    public void setStillInClan(boolean stillInClan) {
        this.stillInClan = stillInClan;
    }
}
