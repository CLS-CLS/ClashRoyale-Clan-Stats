package org.lytsiware.clash.war2.service.integration.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.utils.HashRemovalDeserializer;
import org.lytsiware.clash.war2.service.integration.CrlConstants;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClanDto {

    @JsonDeserialize(using = HashRemovalDeserializer.class)
    String tag;

    String name;

    int fame;

    int repairPoints;

    @JsonFormat(pattern = CrlConstants.DATE_FORMAT)
    LocalDateTime finishTime;

    List<ParticipantDto> participants;
}
