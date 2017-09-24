package org.lytsiware.clash.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class StatsDto implements Serializable{
	
	private static final long serialVersionUID = 1L;
	
	
	private int week;
	private LocalDate startDate;
	private LocalDate endDate;
	
	private int chestContribution;
	private int cardDonation;
	
	
	
	public StatsDto(int week, LocalDate startDate, LocalDate endDate, int chestContribution, int cardDonation) {
		super();
		this.week = week;
		this.startDate = startDate;
		this.endDate = endDate;
		this.chestContribution = chestContribution;
		this.cardDonation = cardDonation;
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
	
	
	
	
	

}
