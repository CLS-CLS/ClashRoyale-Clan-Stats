package org.lytsiware.clash.dto;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.clanweeklystats.ClanWeeklyStats;

import java.time.LocalDate;
import java.util.List;

public class ClanWeeklyStatsDto {

    private int week;
    private LocalDate startDate;
    private LocalDate endDate;
    private double clanChestScore;
    private double crownScore;
    private double playerDeviationScore;
    private List<Integer> data;

    public ClanWeeklyStatsDto() {

    }

    public ClanWeeklyStatsDto(Week week, double clanChestScore, double crownScore, double playerDeviationScore) {
        this.week = week.getWeek();
        this.startDate = week.getStartDate();
        this.endDate = week.getEndDate();
        this.clanChestScore = clanChestScore;
        this.crownScore = crownScore;
        this.playerDeviationScore = playerDeviationScore;
    }

    public static ClanWeeklyStatsDto from(ClanWeeklyStats clanWeeklyStats) {
        if (clanWeeklyStats == null) {
            return null;
        }
        return new ClanWeeklyStatsDto(Week.fromWeek(clanWeeklyStats.getWeek()), clanWeeklyStats.getClanChestScore(), clanWeeklyStats.getCrownScore(), clanWeeklyStats.getPlayerDeviationScore());
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

    public double getCrownScore() {
        return crownScore;
    }

    public void setCrownScore(double crownScore) {
        this.crownScore = crownScore;
    }

    public double getPlayerDeviationScore() {
        return playerDeviationScore;
    }

    public void setPlayerDeviationScore(double playerDeviationScore) {
        this.playerDeviationScore = playerDeviationScore;
    }

    public List<Integer> getData() {
        return data;
    }

    public void setData(List<Integer> data) {
        this.data = data;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
