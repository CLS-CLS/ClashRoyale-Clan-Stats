package org.lytsiware.clash.war2.service;

import org.junit.Test;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.service.integration.War2CRLIntegrationService;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

public class RiverRaceServiceTest {

    private final RiverRaceRepository repository = Mockito.mock(RiverRaceRepository.class);
    private final War2CRLIntegrationService integration = Mockito.mock(War2CRLIntegrationService.class);

    RiverRaceInternalService riverRaceService = new RiverRaceInternalService(integration, repository);

    @Test(expected = SectionIndexMissmatchException.class)
    public void testUpdateCurrent_SectionIndexMissmatch() {
        //given
        Mockito.when(repository.activeRace()).thenReturn(Optional.of(RiverRace.builder().sectionIndex(1).build()));
        Mockito.when(integration.getCurrentRiverRace()).thenReturn(RiverRaceCurrentDto.builder().sectionIndex(2).build());

        //when
        riverRaceService.updateActiveRace();
    }

    @Test(expected = SectionIndexMissmatchException.class)
    public void testFinilize_SectionIndexMissmatch() {
        //given
        Mockito.when(repository.activeRace()).thenReturn(Optional.of(RiverRace.builder().sectionIndex(1).build()));
        Mockito.when(integration.getRiverRaceLog(any())).thenReturn(RiverRaceLogDto.builder()
                .items(Collections.singletonList(RiverRaceLogDto.RiverRaceWeekDto.builder().sectionIndex(2).build())).build());

        //when
        riverRaceService.finalizeRace("");
    }

}