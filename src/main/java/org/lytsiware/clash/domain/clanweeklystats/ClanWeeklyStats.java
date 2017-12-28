package org.lytsiware.clash.domain.clanweeklystats;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ClanWeeklyStats {

    @Id
    private int week;

    private double clanChestScore;

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public double getClanChestScore() {
        return clanChestScore;
    }

    public void setClanChestScore(double clanChestScore) {
        this.clanChestScore = clanChestScore;
    }
}
