package org.lytsiware.clash.war2.service;

import lombok.RequiredArgsConstructor;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.transformation.RiverRaceWebMapper;
import org.lytsiware.clash.war2.web.dto.RiverRaceViewDto;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

/**
 * Exposes operations to be user from the web controller
 */
@Service
@RequiredArgsConstructor
public class RiverRaceWebService {

    private final RiverRaceRepository repository;

    public RiverRaceViewDto getRiverRace(int index) {
        return RiverRaceWebMapper.INSTANCE.toRiverRaceViewDto(repository.getRiverRace(PageRequest.of(index, 1))
                .stream().findFirst().orElse(null));
    }

}
