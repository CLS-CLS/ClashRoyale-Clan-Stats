package org.lytsiware.clash.donation.domain;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class ClanWeeklyStats {

    @Id
    private int week;

    private double clanChestScore;

    private Double crownScore;

    private Double playerDeviationScore;


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

    public void setCrownScore(double crownScore) {
        this.crownScore = crownScore;
    }

    public Double getCrownScore() {
        return crownScore;
    }

    public void setPlayerDeviationScore(double playerDeviationScore) {
        this.playerDeviationScore = playerDeviationScore;
    }
    public Double getPlayerDeviationScore() {
       return playerDeviationScore;
    }
}
