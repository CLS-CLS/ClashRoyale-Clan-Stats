package org.lytsiware.clash.war2.service;

import lombok.RequiredArgsConstructor;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.domain.player.PlayerInOut;
import org.lytsiware.clash.core.domain.player.PlayerRepository;
import org.lytsiware.clash.donation.service.PlayerCheckInService;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.lytsiware.clash.war2.repository.AggregationRepository;
import org.lytsiware.clash.war2.repository.RiverRaceParticipantRepository;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.repository.dto.RiverRaceAggregateDto;
import org.lytsiware.clash.war2.transformation.ParticipantMapper;
import org.lytsiware.clash.war2.transformation.RiverRaceWebMapper;
import org.lytsiware.clash.war2.web.dto.ParticipantDto;
import org.lytsiware.clash.war2.web.dto.RiverRaceViewDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private final AggregationRepository aggregationRepository;
    private final RiverRaceRepository riverRaceRepository;
    private final RiverRaceParticipantRepository riverRaceParticipantRepository;
    private final PlayerCheckInService checkInService;
    private final PromotionService promotionService;

    public RiverRaceViewDto getRiverRace(int index) {
        List<RiverRaceAggregateDto> aggregatedStats = aggregationRepository.getAggregatedStats(index, clanTag);

        //initialize basic info from entity
        RiverRaceViewDto riverRaceDto = RiverRaceWebMapper.INSTANCE.toRiverRaceViewDto(riverRaceRepository.getRiverRaces(PageRequest.of(index, 1))
                .stream().findFirst().orElse(null));

        Map<String, Player> players = playerRepository.loadAll().entrySet().stream()
                .filter(e -> e.getValue().getInClan()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, PlayerInOut> inOut = checkInService.findInClan().stream().collect(Collectors.toMap(PlayerInOut::getTag, i -> i));


        for (ParticipantViewDto participant : riverRaceDto.getClan().getParticipants()) {

            if (inOut.get(participant.getTag()) != null) {
                participant.setDaysInClan((int) ChronoUnit.DAYS.between(inOut.get(participant.getTag()).getCheckIn(), LocalDateTime.now()));
            }

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
                participant.setInClan(player.getInClan());
            } else {
                participant.setRole("Member");
            }
        }

        Collections.sort(riverRaceDto.getClans(), Comparator.comparing(c -> -c.getFame()));
        return riverRaceDto;
    }

    public List<ParticipantDto> getRiverRaceParticipant(String playerTag) {
        List<RiverRaceParticipant> results = riverRaceParticipantRepository.findByTagOrderByRace(playerTag, clanTag);
        List<Integer> promotionPoints = promotionService.calculatePromotion(playerTag);
        return IntStream.range(0, results.size())
                .mapToObj(i -> ParticipantMapper.INSTANCE.toParticipantView(results.get(i), i < promotionPoints.size() ? promotionPoints.get(i) : null))
                .collect(Collectors.toList());
    }

}
