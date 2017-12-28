package org.lytsiware.clash.dto;

import org.lytsiware.clash.domain.clanweeklystats.ClanWeeklyStats;

public class ClanWeeklyStatsDto {

    private int week;
    private double clanChestScore;

    public ClanWeeklyStatsDto(){

    }

    public ClanWeeklyStatsDto(int week, double clanChestScore) {
        this.week = week;
        this.clanChestScore = clanChestScore;
    }

    public static ClanWeeklyStatsDto from (ClanWeeklyStats clanWeeklyStats) {
        if (clanWeeklyStats == null) {
            return null;
        }
        return new ClanWeeklyStatsDto(clanWeeklyStats.getWeek(), clanWeeklyStats.getClanChestScore());
    }

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
