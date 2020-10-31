package org.lytsiware.clash.war2.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.util.Assert;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
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

    public List<Integer> calculatePromotion(String tag) {
        List<RiverRace> riverRaces = riverRaceRepository.getRiverRaces(PageRequest.of(0, 9)).stream()
                .filter(riverRace -> riverRace.getClan().getParticipants().stream().anyMatch(p -> p.getTag().equals(tag)))
                .collect(Collectors.toList());

        List<RiverRaceParticipant> participation = riverRaces.stream().map(RiverRace::getClan)
                .flatMap(c -> c.getParticipants().stream())
                .filter(p -> p.getTag().equals(tag))
                .collect(Collectors.toList());

        if (participation.isEmpty()) {
            log.debug("no participation found for player {}", tag);
            return Collections.emptyList();
        }

        PlayerInOut inOut = checkInService.findByTag(tag).orElse(null);
        if (inOut == null) {
            log.debug("no checking / out  found for player {}", tag);
            return Collections.emptyList();
        }


        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < participation.size(); i++) {
            List<RiverRaceParticipant> subParticipation = new ArrayList<>(participation.subList(i, participation.size()));
            removeNegativeScoreConditionally(inOut, subParticipation, riverRaces, true);
            result.add(subParticipation.stream().mapToInt(RiverRaceParticipant::getScore).map(PromotionDiff::getPromotionPoint).sum());
        }
        return result;
    }


    public List<PlayerPromotionDto> calculatePromotions() {
        log.debug("calculatePromotions");
        List<RiverRace> riverRaces = riverRaceRepository.getRiverRaces(PageRequest.of(0, 9)).stream()
                .collect(Collectors.toList());

        Map<String, List<RiverRaceParticipant>> participation = riverRaces.stream()
                .map(RiverRace::getClan)
                .flatMap(c -> c.getParticipants().stream())
                .collect(Collectors.groupingBy(RiverRaceParticipant::getTag));


        if (participation.isEmpty()) {
            log.debug("calculatePromotions list is empty - return");
            return Collections.emptyList();
        }

        Map<String, PlayerInOut> inOut = checkInService.findInClan().stream().collect(Collectors.toMap(PlayerInOut::getTag, i -> i));

        Map<String, Player> inClanPlayers = playerRepository.findInClan().stream().collect(Collectors.toMap(Player::getTag, p -> p));

        //filter by players in clan
        participation.keySet().stream().filter(p -> !inClanPlayers.containsKey(p))
                .collect(Collectors.toList())
                .forEach(participation::remove);

        //do not calculate the very first week he joined if he joined late and has negative impact
        for (String tag : participation.keySet()) {
            removeNegativeScoreConditionally(inOut.get(tag), participation.get(tag), riverRaces, false);
        }

        return participation.keySet().stream()
                .filter(tag -> !participation.get(tag).isEmpty())
                .map(tag ->
                        buildPlayerPromortionDto(participation.get(tag), inOut.get(tag), inClanPlayers.get(tag)))
                .collect(Collectors.toList());
    }

    private PlayerPromotionDto buildPlayerPromortionDto(List<RiverRaceParticipant> participation, PlayerInOut inOut, Player player) {

        ArrayList<RiverRaceParticipant> excludeActiveParticipationList = new ArrayList<>(participation);

        RiverRaceParticipant activeParticipant = excludeActiveParticipationList.remove(0);

        int activePoint = PromotionDiff.getPromotionPoint(activeParticipant.getScore());

        int points = excludeActiveParticipationList.stream().map(RiverRaceParticipant::getScore)
                .mapToInt(PromotionDiff::getPromotionPoint).sum();
        int totalPoints = activePoint + points;

        PlayerPromotionDto playerPromotionDto = PlayerPromotionDto.builder()
                .tag(player.getTag())
                .name(player.getName())
                .role(player.getRole())
                .promotionPoints(points)
                .daysInClanAtEndOfRace(ChronoUnit.DAYS.between(inOut.getCheckIn(), LocalDateTime.now()))
                .totalPromotionPoints(totalPoints)
                .latestScore(excludeActiveParticipationList.isEmpty() ? null : excludeActiveParticipationList.get(0).getScore())
                .latestActiveScore(activeParticipant.getScore())
                .build();
        return playerPromotionDto;
    }


    /**
     * Removes the very first participation of a player in river race if he joined late (after thursday included) at war.
     * If the very first participation
     * is the active one it does not remove it
     *
     * @param inOut                 the checkin/checkout dates
     * @param participations        the player's stats for each riverrace
     * @param riverRaces            the river races
     * @param allowSingletonRemoval if false it does not remove any participation if the participation list only contains one element
     */
    private void removeNegativeScoreConditionally(PlayerInOut inOut, List<RiverRaceParticipant> participations, List<RiverRace> riverRaces, boolean allowSingletonRemoval) {
        log.debug("removeNegativeScoreConditionally for player {}", Optional.ofNullable(inOut).map(PlayerInOut::getTag).orElse(null));
        Assert.notEmpty(participations, "there must be at least one participation");

        //in case of only one participation = current one = current => do not remove
        if (!allowSingletonRemoval && participations.size() <= 1) {
            log.debug("List size is {}, not removing anything", participations.size());
            return;
        }
        //find the dates if the first participation riverrace
        RiverRaceParticipant firstParticipation = participations.get(participations.size() - 1);
        RiverRace firstParticipationRiverRace = riverRaces.stream()
                .filter(riverRace -> riverRace.getClan().getParticipants().stream()
                        .anyMatch(p -> p == firstParticipation)).findFirst()
                .orElseThrow(() -> new IllegalStateException("could not find riverrace of participant !!"));

        if (firstParticipationRiverRace.getSuperCellCreatedDate() != null
                && firstParticipationRiverRace.getSuperCellCreatedDate().minusDays(7).isBefore(inOut.getCheckIn())
                && PromotionDiff.getPromotionPoint(firstParticipation.getScore()) <= 0
                && Arrays.asList(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(inOut.getCheckIn().getDayOfWeek())) {
            log.debug("Removing participation for player {} at river race at date {}", inOut.getTag(), firstParticipationRiverRace.getSuperCellCreatedDate());
            participations.remove(participations.size() - 1);
        }
    }


}
