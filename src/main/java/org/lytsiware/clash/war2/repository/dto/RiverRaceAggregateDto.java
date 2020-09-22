package org.lytsiware.clash.war2.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RiverRaceAggregateDto {

    public static RiverRaceAggregateDto of(Object[] o) {
        return RiverRaceAggregateDto.builder()
                .tag((String) o[0])
                .name((String) o[1])
                .fame(((BigDecimal) o[2]).intValue())
                .activeFame(((BigDecimal) o[3]).intValue())
                .repairPoints(((BigDecimal) o[4]).intValue())
                .score(((BigDecimal) o[5]).intValue())
                .build();

    }

    String tag;
    String name;
    int fame;
    int activeFame;
    int repairPoints;
    int score;

}
