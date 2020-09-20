package org.lytsiware.clash.war2.transformation;

import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.web.dto.RiverRaceViewDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface RiverRaceWebMapper {

    RiverRaceWebMapper INSTANCE = Mappers.getMapper(RiverRaceWebMapper.class);

    RiverRaceViewDto toRiverRaceViewDto(RiverRace riverRace);
}
