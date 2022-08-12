package org.lytsiware.clash.war2.repository.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RiverRaceAggregateDto {

    public static RiverRaceAggregateDto of(Object[] o) {
        return RiverRaceAggregateDto.builder()
                .tag((String) o[0])
                .fame(((BigDecimal) o[1]).intValue())
                .activeFame(((BigDecimal) o[2]).intValue())
                .repairPoints(((BigDecimal) o[3]).intValue())
                .score(((BigDecimal) o[4]).intValue())
                .totalWarDecks(((BigInteger) o[5]).intValue())
                .totalRequiredDecks(((BigInteger) o[6]).intValue())
                .build();

    }

    String tag;
    int fame;
    int activeFame;
    int repairPoints;
    int score;
    int totalWarDecks;
    int totalRequiredDecks;

}
