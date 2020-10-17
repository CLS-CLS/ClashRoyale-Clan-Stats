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

import java.time.DayOfWeek;
import java.time.LocalDateTime;
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

        public static int getPromotionPoint(int score) {
            return promotionDiffOfScore(score).getPromotionPoint();
        }

        public int getPromotionPoint() {
            return promotionPoint;
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
        private Integer latestScore;
        private Integer promotionPoints;
        private String role;
        private Long daysInClanAtEndOfRace;
        private int latestActiveScore;
        //including active race
        private int totalPromotionPoints;
    }


    public List<PlayerPromotionDto> calculatePromotions() {
        List<RiverRace> riverRaces = riverRaceRepository.getRiverRaces(PageRequest.of(0, 9)).stream()
                .collect(Collectors.toList());

        Map<String, List<RiverRaceParticipant>> participation = riverRaces.stream()
                .map(RiverRace::getClan)
                .flatMap(c -> c.getParticipants().stream())
                .collect(Collectors.groupingBy(RiverRaceParticipant::getTag));


        if (participation.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, PlayerInOut> inOut = checkInService.findInClan().stream().collect(Collectors.toMap(PlayerInOut::getTag, i -> i));

        Map<String, Player> inClanPlayers = playerRepository.findInClan().stream().collect(Collectors.toMap(Player::getTag, p -> p));

        //filter by players in clan
        participation.keySet().stream().filter(p -> !inClanPlayers.containsKey(p)).collect(Collectors.toList())
                .forEach(participation::remove);

        //do not calculate the very first week he joined if he joined late and has negative impact
        Map<RiverRaceParticipant, RiverRace> participationRiveRaceMap;
        participation.keySet().forEach(tag -> filterOutVeryFirstWeekConditionally(participation.get(tag), inOut.get(tag)));
        participation.keySet().stream().filter(key -> participation.get(key).isEmpty()).collect(Collectors.toList())
                .forEach(participation::remove);


        List<PlayerPromotionDto> result = new ArrayList<>();
        for (String tag : participation.keySet()) {
            ArrayList<RiverRaceParticipant> excludeActive = new ArrayList<>(participation.get(tag));

            RiverRaceParticipant activeParticipant = excludeActive.remove(0);

            int activePoint = PromotionDiff.getPromotionPoint(activeParticipant.getScore());

            int points = excludeActive.stream().map(RiverRaceParticipant::getScore)
                    .mapToInt(PromotionDiff::getPromotionPoint).sum();
            int totalPoints = activePoint + points;

            PlayerPromotionDto playerPromotionDto = PlayerPromotionDto.builder()
                    .tag(tag)
                    .name(inClanPlayers.get(tag).getName())
                    .promotionPoints(points)
                    .totalPromotionPoints(totalPoints)
                    .latestScore(excludeActive.isEmpty() ? null : excludeActive.get(0).getScore())
                    .role(inClanPlayers.get(tag).getRole())
                    .daysInClanAtEndOfRace(ChronoUnit.DAYS.between(inOut.get(tag).getCheckIn(), LocalDateTime.now()))
                    .latestActiveScore(activeParticipant.getScore())
                    .build();

            result.add(playerPromotionDto);

        }
        return result;

    }

    private void filterOutVeryFirstWeekConditionally(List<RiverRaceParticipant> riverRaceParticipants, PlayerInOut playerInOut) {
        //empty or active race
        if (riverRaceParticipants.size() <= 1) {
            return;
        }
        RiverRaceParticipant firstParticipation = riverRaceParticipants.get(riverRaceParticipants.size() - 1);
        //do not count negative score if player joined at or after thursday
        if (PromotionDiff.getPromotionPoint(firstParticipation.getScore()) <= 0 &&
                Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(playerInOut.getCheckIn().getDayOfWeek())) {
            riverRaceParticipants.remove(riverRaceParticipants.size() - 1);
        }

    }


}
