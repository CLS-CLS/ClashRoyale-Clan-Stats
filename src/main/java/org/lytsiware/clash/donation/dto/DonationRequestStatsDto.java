package org.lytsiware.clash.donation.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class DonationRequestStatsDto implements Serializable {

    private static final long serialVersionUID = 1L;


    private int week;
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer chestContribution;
    private Integer cardDonation;
    private Integer cardsReceived;


    public DonationRequestStatsDto(int week, LocalDate startDate, LocalDate endDate, Integer chestContribution, Integer cardDonation, Integer cardsReceived) {
        super();
        this.week = week;
        this.startDate = startDate;
        this.endDate = endDate;
        this.chestContribution = chestContribution;
        this.cardDonation = cardDonation;
        this.cardsReceived = cardsReceived;
    }


    public int getWeek() {
        return week;
    }


    public void setWeek(int week) {
        this.week = week;
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


    public Integer getCardsReceived() {
        return cardsReceived;
    }


    public void setCardsReceived(Integer cardsReceived) {
        this.cardsReceived = cardsReceived;
    }


}
