package org.lytsiware.clash.war2.service.integration.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.war2.service.integration.CrlConstants;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiverRaceLogDto {

    List<RiverRaceWeekDto> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiverRaceWeekDto {
        int sectionIndex;
        int seasonId;

        @JsonFormat(pattern = CrlConstants.DATE_FORMAT)
        LocalDateTime createdDate;
        List<StandingsDto> standings;

    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StandingsDto {
        private int rank;
        private int trophyChange;
        private ClanDto clan;
    }


}


