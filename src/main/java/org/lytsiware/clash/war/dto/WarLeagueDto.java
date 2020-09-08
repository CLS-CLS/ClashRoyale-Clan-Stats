package org.lytsiware.clash.war.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.war.domain.league.WarLeague;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WarLeagueDto {

    private LocalDate startDate;

    private LocalTime time;

    private Integer teamCardAvg;

    private Integer teamTotalCards;

    private Integer totalTrophies;

    public WarLeagueDto(WarLeague warLeague) {
        this.startDate = warLeague.getStartDate();
        this.time = warLeague.getTime();
        this.teamCardAvg = warLeague.getTeamCardAvg();
        this.teamTotalCards = warLeague.getTeamTotalCards();
        this.totalTrophies = warLeague.getTotalTrophies();
    }
}
