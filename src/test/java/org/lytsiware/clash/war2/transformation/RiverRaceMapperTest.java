package org.lytsiware.clash.war2.transformation;

import org.junit.Assert;
import org.junit.Test;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.domain.RiverRaceClan;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.lytsiware.clash.war2.service.integration.dto.ClanDto;
import org.lytsiware.clash.war2.service.integration.dto.ParticipantDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RiverRaceMapperTest {


    @Test
    public void updateCurrentTest() {
        RiverRaceCurrentDto dto = riverRaceDto();
        RiverRace entity = RiverRace.builder().build();
        RiverRaceInternalMapper.INSTANCE.update(dto, entity);

        Assert.assertEquals(2, entity.getClans().size());
        Assert.assertEquals(2, entity.getClans().get(0).getParticipants().size());
        Assert.assertEquals(100, entity.getClan().getFame());
        Assert.assertEquals(LocalDateTime.of(2020, 5, 5, 5, 5), entity.getClan().getFinishTime());
        Assert.assertEquals(2, entity.getClan().getParticipants().size());
        Assert.assertEquals(100, entity.getClan().getParticipants().get(0).getFame());
        Assert.assertEquals("Clan1", entity.getClan().getName());
        Assert.assertEquals("PG18", entity.getClan().getTag());
        Assert.assertEquals("PG19", entity.getClans().get(1).getTag());
        Assert.assertEquals(50, entity.getClans().get(0).getParticipants().get(0).getRepairPoints());
        Assert.assertEquals(100, entity.getClans().get(0).getParticipants().get(0).getFame());
        Assert.assertEquals("A", entity.getClans().get(0).getParticipants().get(0).getTag());
        Assert.assertEquals(1, entity.getSectionIndex());
    }


    @Test
    public void updateLogTest() {
        RiverRaceLogDto dto = riverRaceLogDto();
        RiverRace entity = riverRaceEntity();
        RiverRaceInternalMapper.INSTANCE.updateFromRiverRaceWeekDto(dto.getItems().get(0), entity, "PG18");

        Assert.assertEquals(20, entity.getSeasonId());
        Assert.assertEquals(2, entity.getSectionIndex());
        Assert.assertEquals(LocalDateTime.of(2020, 9, 19, 16, 0), entity.getSuperCellCreatedDate());
        Assert.assertEquals(100, entity.getClan().getFame());
        Assert.assertEquals(3, entity.getClan().getParticipants().size());
        Assert.assertEquals("A", entity.getClan().getParticipants().get(0).getTag());
        Assert.assertEquals(100, entity.getClan().getParticipants().get(0).getActiveFame());
        Assert.assertEquals(1, entity.getClans().size());
        Assert.assertEquals(2, entity.getClans().get(0).getParticipants().size());
        Assert.assertEquals(1, entity.getClan().getRank());
        Assert.assertEquals(20, entity.getClan().getTrophyChange());
    }

    @Test
    public void updateActiveFame() {
        RiverRaceCurrentDto dto = riverRaceDto();
        RiverRace entity = RiverRace.builder().build();
        RiverRaceInternalMapper.INSTANCE.update(dto, entity);
        RiverRaceInternalMapper.INSTANCE.updateActiveFame(dto.getClan(), entity.getClan());

        Assert.assertEquals(200, entity.getClan().getParticipants().get(1).getFame());
        Assert.assertEquals(200, entity.getClan().getParticipants().get(1).getActiveFame());
    }

    private RiverRace riverRaceEntity() {
        return RiverRace.builder()
                .clan(RiverRaceClan.builder()
                        .fame(50)
                        .tag("PG18")
                        .participants(Stream.of(RiverRaceParticipant.builder()
                                        .activeFame(100)
                                        .fame(200)
                                        .tag("A")
                                        .build(),
                                RiverRaceParticipant.builder()
                                        .activeFame(10)
                                        .fame(100)
                                        .tag("B")
                                        .build(),
                                RiverRaceParticipant.builder()
                                        .activeFame(10)
                                        .fame(100)
                                        .tag("AC")
                                        .build()
                        ).collect(Collectors.toList()))
                        .build()
                )
                .sectionIndex(2)
                .clans(Stream.of(
                        RiverRaceClan.builder()
                                .tag("PG19")
                                .build()
                ).collect(Collectors.toList()))
                .build();
    }

    private RiverRaceLogDto riverRaceLogDto() {
        return RiverRaceLogDto.builder()
                .items(items())
                .build();
    }

    private List<RiverRaceLogDto.RiverRaceWeekDto> items() {
        return Stream.of(
                RiverRaceLogDto.RiverRaceWeekDto.builder()
                        .createdDate(LocalDate.of(2020, 9, 19).atTime(16, 0))
                        .seasonId(20)
                        .sectionIndex(3)
                        .standings(standings())
                        .build()
        ).collect(Collectors.toList());
    }

    private List<RiverRaceLogDto.StandingsDto> standings() {
        return Stream.of(
                RiverRaceLogDto.StandingsDto.builder()
                        .clan(clan())
                        .rank(1)
                        .trophyChange(20)
                        .build(),
                RiverRaceLogDto.StandingsDto.builder()
                        .clan(clan2())
                        .rank(1)
                        .trophyChange(20)
                        .build()
        ).collect(Collectors.toList());
    }

    private RiverRaceCurrentDto riverRaceDto() {
        return RiverRaceCurrentDto.builder()
                .sectionIndex(1)
                .clan(clan())
                .clans(clans())
                .build();
    }

    private List<ClanDto> clans() {
        List<ClanDto> clans = new ArrayList<>();
        clans.add(clan());
        clans.add(clan2());
        return clans;
    }

    private ClanDto clan() {
        return ClanDto.builder()
                .fame(100)
                .name("Clan1")
                .tag("PG18")
                .repairPoints(100)
                .participants(participants())
                .finishTime(LocalDateTime.of(2020, 5, 5, 5, 5))
                .build();
    }

    private ClanDto clan2() {
        return ClanDto.builder()
                .fame(10)
                .name("Clan2")
                .tag("PG19")
                .repairPoints(10)
                .participants(participants())
                .finishTime(LocalDateTime.of(2020, 5, 6, 5, 5))
                .build();
    }

    private List<ParticipantDto> participants() {
        return Stream.of(ParticipantDto.builder()
                        .fame(100)
                        .repairPoints(50)
                        .tag("A")
                        .name("A").build(),
                ParticipantDto.builder()
                        .fame(200)
                        .repairPoints(60)
                        .tag("B")
                        .name("BA").build()).collect(Collectors.toList());

    }
}