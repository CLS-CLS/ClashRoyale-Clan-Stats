package org.lytsiware.clash.war2.transformation;

import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.domain.RiverRaceClan;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.lytsiware.clash.war2.service.integration.dto.ClanDto;
import org.lytsiware.clash.war2.service.integration.dto.ParticipantDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Mapper
public interface RiverRaceInternalMapper {

    RiverRaceInternalMapper INSTANCE = Mappers.getMapper(RiverRaceInternalMapper.class);

    void update(RiverRaceCurrentDto dto, @MappingTarget RiverRace riverRace);

    default void updateActiveFame(RiverRaceCurrentDto dto, RiverRace activeRace) {
        Optional.ofNullable(dto.getClan().getParticipants())
                .orElse(new ArrayList<>())
                .forEach(participantDto -> Optional.ofNullable(activeRace.getClan())
                        .map(RiverRaceClan::getParticipants)
                        .orElse(new ArrayList<>())
                        .stream()
                        .filter(participant -> participantDto.getTag().equals(participant.getTag()))
                        .findFirst()
                        .ifPresent(currentParticipant -> currentParticipant.setActiveFame(participantDto.getFame()))
                );
    }

    default void updateFromRiverRaceWeekDto(RiverRaceLogDto.RiverRaceWeekDto dto, RiverRace riverRace, String clanTag) {
        riverRace.setSeasonId(dto.getSeasonId());
        riverRace.setSuperCellCreatedDate(dto.getCreatedDate());

        RiverRaceLogDto.StandingsDto myClanStandings = dto.getStandings().stream()
                .filter(rrl -> rrl.getClan().getTag().equals(clanTag))
                .findFirst().orElseThrow(() -> new RuntimeException("Clan not found"));

        List<RiverRaceLogDto.StandingsDto> otherStandings = dto.getStandings().stream()
                .filter(rrl -> !rrl.getClan().getTag().equals(clanTag))
                .collect(Collectors.toList());

        //in case no clan or clans exist (only when migrating data so active race was never recorded)
        initializeClans(riverRace, clanTag, otherStandings);

        updateFromClanDto(myClanStandings.getClan(), riverRace.getClan());
        updateOtherRiverRaceClans(otherStandings, riverRace.getClans());

        riverRace.getClan().setTrophyChange(myClanStandings.getTrophyChange());
        riverRace.getClan().setRank(myClanStandings.getRank());

    }

    default void initializeClans(RiverRace riverRace, String clanTag, List<RiverRaceLogDto.StandingsDto> otherStandings) {
        if (riverRace.getClan() == null) {
            riverRace.setClan(RiverRaceClan.builder()
                    .tag(clanTag)
                    .build());
        }
        if (riverRace.getClans() == null) {
            riverRace.setClans(otherStandings.stream().map(RiverRaceLogDto.StandingsDto::getClan).map(clanDto ->
                    RiverRaceClan.builder().tag(clanDto.getTag()).build()).collect(Collectors.toList()));
        }
    }

    default void updateOtherRiverRaceClans(List<RiverRaceLogDto.StandingsDto> otherClans, List<RiverRaceClan> clans) {
        otherClans.forEach(dto -> clans.stream()
                .filter(riverRaceClan -> riverRaceClan.getTag().equals(dto.getClan().getTag()))
                .findFirst()
                .ifPresent(riverRaceClan -> {
                            updateFromClanDto(dto.getClan(), riverRaceClan);
                            riverRaceClan.setRank(dto.getRank());
                            riverRaceClan.setTrophyChange(dto.getTrophyChange());
                        }
                ));
    }

    /**
     * updated the current participants or adds new ones
     */
    default void updateParticipantList(List<ParticipantDto> participantDtos, @MappingTarget List<RiverRaceParticipant> participants) {
        for (ParticipantDto participantDto : participantDtos) {
            RiverRaceParticipant participant = participants.stream()
                    .filter(rrp -> participantDto.getTag().equals(rrp.getTag()))
                    .findFirst()
                    .orElse(null);
            if (participant != null) {
                updateParticipant(participantDto, participant);
            } else {
                participants.add(toRiverRaceParticipant(participantDto));
            }
        }
    }

    void updateParticipant(ParticipantDto participantDto, @MappingTarget RiverRaceParticipant participant);

    void updateFromClanDto(ClanDto dto, @MappingTarget RiverRaceClan entityClan);

    RiverRaceParticipant toRiverRaceParticipant(ParticipantDto participantDto);

}
