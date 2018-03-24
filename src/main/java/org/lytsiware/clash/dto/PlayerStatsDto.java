package org.lytsiware.clash.dto;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;

import java.util.ArrayList;
import java.util.List;

public class PlayerStatsDto {

	String name;
	String tag;

	List<StatsDto> statsDto;

	public PlayerStatsDto() {
		super();
	}

	public PlayerStatsDto(String name, String tag, List<StatsDto> statsDto) {
		super();
		this.name = name;
		this.tag = tag;
		this.statsDto = statsDto;
	}

	public PlayerStatsDto(String name, String tag) {
		super();
		this.name = name;
		this.tag = tag;
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

	public List<StatsDto> getStatsDto() {
		return statsDto;
	}

	public void setStatsDto(List<StatsDto> statsDto) {
		this.statsDto = statsDto;
	}

	public static PlayerStatsDto toPlayerStatsDto(List<PlayerWeeklyStats> playerStats) {
		List<StatsDto> statsDto = new ArrayList<>();
		PlayerStatsDto playerStatsDto = null;
		for (PlayerWeeklyStats pws :playerStats ) {
			if (playerStatsDto == null) {
				playerStatsDto = new PlayerStatsDto(pws.getPlayer().getName(), pws.getPlayer().getTag(), statsDto);
			}
			StatsDto stat = new StatsDto(pws.getWeek(), Week.fromWeek(pws.getWeek()).getStartDate(),
                    Week.fromWeek(pws.getWeek()).getEndDate(), pws.getChestContribution(), pws.getCardDonation(), pws.getCardsReceived());
            statsDto.add(stat);
		}
		return playerStatsDto;
	}

}
