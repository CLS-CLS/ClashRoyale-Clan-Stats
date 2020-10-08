package org.lytsiware.clash.war2.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.domain.player.PlayerRepository;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.domain.RiverRaceClan;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.service.integration.War2CRLIntegrationService;
import org.lytsiware.clash.war2.service.integration.dto.ClanDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;

public class RiverRaceServiceTest {

    private final RiverRaceRepository repository = Mockito.mock(RiverRaceRepository.class);
    private final War2CRLIntegrationService integration = Mockito.mock(War2CRLIntegrationService.class);
    private final PlayerRepository playerRepository = Mockito.mock(PlayerRepository.class);

    RiverRaceInternalService riverRaceService = new RiverRaceInternalService(integration, repository, playerRepository);

    @Before
    public void init() {
        Mockito.when(playerRepository.findInClan()).thenReturn(new ArrayList<>());
    }

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

    @Test
    public void testRiverRaceInsertsGhostPlayers() {
        //given
        RiverRace riverRace = RiverRace.builder().sectionIndex(1)
                .clan(RiverRaceClan.builder()
                        .participants(Stream.of(RiverRaceParticipant.builder()
                                .tag("asd").build()).collect(Collectors.toList()))
                        .build())
                .clans(new ArrayList<>())
                .build();

        Mockito.when(repository.activeRace()).thenReturn(Optional.of(riverRace));
        Mockito.when(integration.getCurrentRiverRace()).thenReturn(RiverRaceCurrentDto.builder()
                .sectionIndex(1)
                .clan(ClanDto.builder()
                        .tag("asd")
                        .participants(new ArrayList<>())
                        .build())
                .clans(new ArrayList<>())
                .build());
        Mockito.when(playerRepository.findInClan()).thenReturn(Stream.of(new Player("ghost", "ghost", "member", true)).collect(Collectors.toList()));
        //when
        riverRaceService.updateActiveRace();

        //then
        Assert.assertEquals(2, riverRace.getClan().getParticipants().size());
        Assert.assertTrue(riverRace.getClan().getParticipants().stream().filter(p -> "ghost".equals(p.getTag())).findFirst().isPresent());

    }

}