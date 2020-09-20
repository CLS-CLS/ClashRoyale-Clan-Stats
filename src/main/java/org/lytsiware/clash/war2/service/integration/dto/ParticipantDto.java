package org.lytsiware.clash.war2.service.integration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.utils.HashRemovalDeserializer;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParticipantDto {

    @JsonDeserialize(using = HashRemovalDeserializer.class)
    String tag;

    String name;

    int fame;

    int repairPoints;
}