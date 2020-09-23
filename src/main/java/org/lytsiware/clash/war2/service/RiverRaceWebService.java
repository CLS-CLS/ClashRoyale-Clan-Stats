package org.lytsiware.clash.war2.service;

import lombok.RequiredArgsConstructor;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.domain.player.PlayerRepository;
import org.lytsiware.clash.war2.repository.AggratationRepository;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.repository.dto.RiverRaceAggregateDto;
import org.lytsiware.clash.war2.transformation.RiverRaceWebMapper;
import org.lytsiware.clash.war2.web.dto.RiverRaceViewDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.lytsiware.clash.war2.web.dto.RiverRaceViewDto.ParticipantViewDto;

/**
 * Exposes operations to be user from the web controller
 */
@Service
@RequiredArgsConstructor
public class RiverRaceWebService {

    @Value("${clanTag}")
    private String clanTag;

    private final PlayerRepository playerRepository;
    private final AggratationRepository repository;
    private final RiverRaceRepository riverRaceRepository;

    public RiverRaceViewDto getRiverRace(int index) {
        List<RiverRaceAggregateDto> aggregatedStats = repository.getAggregatedStats(index, clanTag);

        RiverRaceViewDto riverRaceDto = RiverRaceWebMapper.INSTANCE.toRiverRaceViewDto(riverRaceRepository.getRiverRace(PageRequest.of(index, 1))
                .stream().findFirst().orElse(null));

        Map<String, Player> players = playerRepository.loadAll().entrySet().stream()
                .filter(e -> e.getValue().getInClan()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));


        for (ParticipantViewDto participant : riverRaceDto.getClan().getParticipants()) {
            RiverRaceAggregateDto s = aggregatedStats.stream()
                    .filter(aggr -> aggr.getTag().equals(participant.getTag()))
                    .findFirst()
                    .orElse(null);
            if (s != null) {
                participant.setAverageFame(s.getFame());
                participant.setAverageActiveFame(s.getActiveFame());
                participant.setAverageRepairPoints(s.getRepairPoints());
                participant.setAverageScore(s.getScore());
            }
            if (players.containsKey(participant.getTag())) {
                Player player = players.get(participant.getTag());
                participant.setRole(player.getRole() != null ? player.getRole() : "Member");
                participant.setName(player.getName());
                participant.setInClan(participant.isInClan());
            } else {
                participant.setRole("Member");
            }
        }
        return riverRaceDto;
    }

}
