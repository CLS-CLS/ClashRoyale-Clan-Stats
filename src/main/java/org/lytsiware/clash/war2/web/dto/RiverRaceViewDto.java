package org.lytsiware.clash.war2.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RiverRaceViewDto {

    private int sectionIndex;

    private int seasonId;

    private ClanViewDto clan;

    private List<ClanViewDto> clans;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClanViewDto {

        private String tag;
        private String name;
        private int trophies;
        private Integer trophyChange;
        private int fame;
        private int repairPoints;
        private LocalDateTime finishTime;
        private Integer rank;
        private List<ParticipantViewDto> participants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ParticipantViewDto {
        private String tag;
        private String name;
        private String rank;
        private int fame;
        private int repairPoints;
        private int activeFame;

    }

}
