package org.lytsiware.clash.war2.service;

import lombok.*;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.domain.player.PlayerInOut;
import org.lytsiware.clash.core.domain.player.PlayerRepository;
import org.lytsiware.clash.donation.service.PlayerCheckInService;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PromotionService {


    @Value("${clanTag}")
    private String clanTag;

    private final PlayerCheckInService checkInService;
    private final RiverRaceRepository riverRaceRepository;
    private final PlayerRepository playerRepository;

    @AllArgsConstructor
    @Getter
    enum PromotionDiff {

        SUPER_UP(2, 1000, 50000), UP(1, 600, 1000), NEUTRAL(0, 400, 600),
        DOWN(-1, 200, 400), SUPER_DOWN(-2, 0, 200);

        int promotionPoint;
        int leftInclusive;
        int rightExclusive;

        public static PromotionDiff promotionDiffOfScore(int score) {
            return Arrays.stream(PromotionDiff.values()).filter(p -> p.contains(score)).findFirst().orElse(null);
        }

        public boolean contains(int score) {
            return score >= leftInclusive && score < rightExclusive;
        }

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @Builder
    public static class PlayerPromotionDto {
        private String tag;
        private String name;
        private int latestScore;
        private int promotionPoints;
        private String role;
        private Long daysInClanAtEndOfRace;
    }


    public List<PlayerPromotionDto> calculatePromotions() {
        //get finished races, promotion will be calculated on finished ones
        List<RiverRace> riverRaces = riverRaceRepository.getRiverRaces(PageRequest.of(0, 12)).stream()
                .filter(riverRace -> riverRace.getSuperCellCreatedDate() != null)
                .collect(Collectors.toList());

        Map<String, List<RiverRaceParticipant>> participation = riverRaces.stream()
                .map(RiverRace::getClan)
                .flatMap(c -> c.getParticipants().stream())
                .collect(Collectors.groupingBy(RiverRaceParticipant::getTag));

        if (participation.isEmpty()) {
            Collections.emptyList();
        }
        Map<String, PlayerInOut> inOut = checkInService.findInClan().stream().collect(Collectors.toMap(PlayerInOut::getTag, i -> i));

        Map<String, Player> inClanPlayers = playerRepository.findInClan().stream().collect(Collectors.toMap(Player::getTag, p -> p));

        //filter by players in clan
        participation.keySet().stream().filter(p -> !inClanPlayers.containsKey(p)).collect(Collectors.toList())
                .forEach(participation::remove);


        List<PlayerPromotionDto> result = new ArrayList<>();
        for (String tag : participation.keySet()) {
            int points = participation.get(tag).stream().map(RiverRaceParticipant::getScore)
                    .map(PromotionDiff::promotionDiffOfScore).mapToInt(PromotionDiff::getPromotionPoint).sum();

            PlayerPromotionDto playerPromotionDto = PlayerPromotionDto.builder()
                    .tag(tag)
                    .name(inClanPlayers.get(tag).getName())
                    .promotionPoints(points)
                    .latestScore(participation.get(tag).get(0).getScore())
                    .role(inClanPlayers.get(tag).getRole())
                    .daysInClanAtEndOfRace(ChronoUnit.DAYS.between(inOut.get(tag).getCheckIn(), riverRaces.get(0).getSuperCellCreatedDate()))
                    .build();

            result.add(playerPromotionDto);

        }
        return result;

    }
}
