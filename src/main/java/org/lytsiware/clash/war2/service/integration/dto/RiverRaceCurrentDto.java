package org.lytsiware.clash.war2.service.integration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiverRaceCurrentDto {

    LocalDateTime updateTime;

    ClanDto clan;

    List<ClanDto> clans;

    int sectionIndex;

    String periodType;


}
