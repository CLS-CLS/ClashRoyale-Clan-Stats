package org.lytsiware.clash.dto;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;

import java.io.Serializable;
import java.time.LocalDate;

public class PlayerOverallStats implements Serializable {

	private static final long serialVersionUID = 1L;
	
	
	private String tag;
    private String name;
    private int week;
    private double avgChestContribution;
    private double avgCardDonation;
    private int chestContribution;
    private int cardDonation;
    private LocalDate startDate;
    private LocalDate endDate;
    private String role;

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

    public PlayerOverallStats(PlayerWeeklyStats playerWeeklyStats) {
        this.tag = playerWeeklyStats.getPlayer().getTag();
        this.name = playerWeeklyStats.getPlayer().getName();
        this.week = playerWeeklyStats.getWeek();
        this.avgCardDonation = playerWeeklyStats.getAvgCardDonation();
        this.avgChestContribution = playerWeeklyStats.getAvgChestContribution();
        this.chestContribution = playerWeeklyStats.getChestContribution();
        this.cardDonation = playerWeeklyStats.getCardDonation();
        Week week = new Week(playerWeeklyStats.getWeek());
        this.startDate = week.getStartDate();
        this.endDate = week.getEndDate();
        this.role = playerWeeklyStats.getPlayer().getRole();

    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public double getAvgChestContribution() {
        return avgChestContribution;
    }

    public void setAvgChestContribution(double avgChestContribution) {
        this.avgChestContribution = avgChestContribution;
    }

    public double getAvgCardDonation() {
        return avgCardDonation;
    }

    public void setAvgCardDonation(double avgCardDonation) {
        this.avgCardDonation = avgCardDonation;
    }

    public int getChestContribution() {
        return chestContribution;
    }

    public void setChestContribution(int chestContribution) {
        this.chestContribution = chestContribution;
    }

    public int getCardDonation() {
        return cardDonation;
    }

    public void setCardDonation(int cardDonation) {
        this.cardDonation = cardDonation;
    }

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}
    
    
}
