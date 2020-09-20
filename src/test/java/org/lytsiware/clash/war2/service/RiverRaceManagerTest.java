package org.lytsiware.clash.war2.service;

import org.junit.Test;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.service.integration.War2CRLIntegrationService;
import org.lytsiware.clash.war2.service.integration.dto.ClanDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

public class RiverRaceManagerTest {

    private final War2CRLIntegrationService integration = Mockito.mock(War2CRLIntegrationService.class);
    private final RiverRaceRepository repository = Mockito.mock(RiverRaceRepository.class);
    private final RiverRaceInternalService service = Mockito.mock(RiverRaceInternalService.class);

    RiverRaceManager manger = new RiverRaceManager(service, repository, integration);


    @Test
    public void testUpdateRiverRace_happy_path_no_finalize() {
        //given
        Mockito.when(integration.getCurrentRiverRace()).thenReturn(RiverRaceCurrentDto.builder()
                .clan(ClanDto.builder().participants(new ArrayList<>()).build())
                .sectionIndex(0).build());
        Mockito.when(repository.activeRace()).thenReturn(Optional.of(RiverRace.builder().sectionIndex(0).build()));

        //when
        manger.updateRiverRace();

        //then
        Mockito.verify(service, Mockito.times(0)).finalizeRace(any());

    }

    @Test
    public void testUpdateRiverRace_happy_path_with_finalize() {
        //given
        Mockito.when(integration.getCurrentRiverRace()).thenReturn(RiverRaceCurrentDto.builder()
                .clan(ClanDto.builder().participants(new ArrayList<>()).build())
                .sectionIndex(1).build());

        Mockito.when(repository.activeRace()).thenReturn(Optional.of(RiverRace.builder().sectionIndex(0).build()));

        //when
        manger.updateRiverRace();

        //then
        Mockito.verify(service, Mockito.times(1)).finalizeRace(any());

    }

}