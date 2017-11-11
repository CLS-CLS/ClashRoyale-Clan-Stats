package org.lytsiware.clash.dto;

import java.util.ArrayList;
import java.util.List;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;

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

	public static PlayerStatsDto toPlayerStatsDto(Player player, List<PlayerWeeklyStats> playerStats) {
		List<StatsDto> statsDto = new ArrayList<>();
		for (PlayerWeeklyStats pws :playerStats ) {
			StatsDto stat = new StatsDto(pws.getWeek(), new Week(pws.getWeek()).getStartDate(), 
					new Week(pws.getWeek()).getEndDate(), pws.getChestContribution(), pws.getCardDonation());
			statsDto.add(stat);
		}
		PlayerStatsDto playerStatsDto = new PlayerStatsDto(player.getName(), player.getTag(), statsDto);
		return playerStatsDto;
	}

}
