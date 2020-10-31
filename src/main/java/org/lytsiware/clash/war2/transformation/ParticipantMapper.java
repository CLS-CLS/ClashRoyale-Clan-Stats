package org.lytsiware.clash.war2.transformation;

import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.lytsiware.clash.war2.web.dto.ParticipantDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ParticipantMapper {

    ParticipantMapper INSTANCE = Mappers.getMapper(ParticipantMapper.class);


    ParticipantDto toParticipantView(RiverRaceParticipant riverRaceParticipant, Integer promotionPoints);
}
