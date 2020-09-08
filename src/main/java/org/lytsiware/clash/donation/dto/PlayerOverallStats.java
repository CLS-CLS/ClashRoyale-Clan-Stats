package org.lytsiware.clash.donation.dto;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.donation.domain.PlayerWeeklyStats;

import java.io.Serializable;
import java.time.LocalDate;

public class PlayerOverallStats implements Serializable {

	private static final long serialVersionUID = 1L;

    private boolean inClan;
    private String tag;
    private String name;
    private int week;
    private double avgChestContribution;
    private double avgCardDonation;
    private double avgCardsReceived;
    private Integer chestContribution;
    private Integer cardDonation;
    private Integer cardsReceived;
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

    public PlayerOverallStats() {
    }

    public PlayerOverallStats(PlayerWeeklyStats playerWeeklyStats) {
        this.tag = playerWeeklyStats.getPlayer().getTag();
        this.name = playerWeeklyStats.getPlayer().getName();
        this.week = playerWeeklyStats.getWeek();
        this.avgCardDonation = playerWeeklyStats.getAvgCardDonation();
        this.avgChestContribution = playerWeeklyStats.getAvgChestContribution();
        this.chestContribution = playerWeeklyStats.getChestContribution();
        this.cardDonation = playerWeeklyStats.getCardDonation();
        Week week = Week.fromWeek(playerWeeklyStats.getWeek());
        this.startDate = week.getStartDate();
        this.endDate = week.getEndDate();
        this.role = playerWeeklyStats.getPlayer().getRole();
        this.cardsReceived = playerWeeklyStats.getCardsReceived();
        this.avgCardsReceived = playerWeeklyStats.getAvgCardsReceived();
        this.inClan = playerWeeklyStats.getPlayer().getInClan();
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

	public Integer getChestContribution() {
		return chestContribution;
	}

	public void setChestContribution(Integer chestContribution) {
		this.chestContribution = chestContribution;
	}

	public Integer getCardDonation() {
		return cardDonation;
	}

	public void setCardDonation(Integer cardDonation) {
		this.cardDonation = cardDonation;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

    public Integer getCardsReceived() {
        return cardsReceived;
    }

    public double getAvgCardsReceived() {
        return avgCardsReceived;
    }

    public void setAvgCardsReceived(double avgCardsReceived) {
        this.avgCardsReceived = avgCardsReceived;
    }

    public boolean isInClan() {
        return inClan;
    }

    public void setInClan(boolean inClan) {
        this.inClan = inClan;
    }

    public void setCardsReceived(Integer cardsReceived) {
        this.cardsReceived = cardsReceived;
    }
}
