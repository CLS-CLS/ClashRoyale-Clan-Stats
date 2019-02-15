package org.lytsiware.clash.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;

public class PlayerStatsDto {

	String name;
	String tag;

	LocalDate joinedAt;

	List<DonationRequestStatsDto> statsDto;


	public PlayerStatsDto(String name, String tag, List<DonationRequestStatsDto> statsDto, LocalDate joinedAt) {
		super();
		this.name = name;
		this.tag = tag;
		this.statsDto = statsDto;
		this.joinedAt = joinedAt;
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public List<DonationRequestStatsDto> getStatsDto() {
		return statsDto;
	}

	public void setStatsDto(List<DonationRequestStatsDto> statsDto) {
		this.statsDto = statsDto;
	}

	public LocalDate getJoinedAt() {
		return joinedAt;
	}

	public void setJoinedAt(LocalDate joinedAt) {
		this.joinedAt = joinedAt;
	}

	public static PlayerStatsDto toPlayerStatsDto(List<PlayerWeeklyStats> playerStats, LocalDate joinedAt) {
		List<DonationRequestStatsDto> statsDto = new ArrayList<>();
		PlayerStatsDto playerStatsDto = null;
		for (PlayerWeeklyStats pws :playerStats ) {
			if (playerStatsDto == null) {
				playerStatsDto = new PlayerStatsDto(pws.getPlayer().getName(), pws.getPlayer().getTag(), statsDto, joinedAt);
			}
			DonationRequestStatsDto stat = new DonationRequestStatsDto(pws.getWeek(), Week.fromWeek(pws.getWeek()).getStartDate(),
                    Week.fromWeek(pws.getWeek()).getEndDate(), pws.getChestContribution(), pws.getCardDonation(), pws.getCardsReceived());
            statsDto.add(stat);
		}
		return playerStatsDto;
	}

}
