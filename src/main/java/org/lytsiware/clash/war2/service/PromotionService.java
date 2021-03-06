package org.lytsiware.clash.war2.service;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.OBJECT)
    public enum PromotionDiff {

        SUPER_UP(2, 1000, 50000), UP(1, 800, 1000), NEUTRAL(0, 600, 800),
        DOWN(-1, 500, 600), SUPER_DOWN(-2, 0, 500);

        private static final float modifier = 1.0f;

        int promotionPoint;
        int leftInclusive;
        int rightExclusive;

        public static PromotionDiff promotionDiffOfScore(int score) {
            return Arrays.stream(PromotionDiff.values()).filter(p -> p.contains(score)).findFirst().orElse(null);
        }

        public int getLeftInclusive() {
            return (int) (leftInclusive * modifier);
        }

        public int getRightExclusive() {
            return (int) (rightExclusive * modifier);
        }

        public static int getPromotionPoint(int score) {
            return promotionDiffOfScore(score).getPromotionPoint();
        }

        public int getPromotionPoint() {
            return promotionPoint;
        }

        public boolean contains(int score) {
            return score >= getLeftInclusive() && score < getRightExclusive();
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

    private List<Integer> doCalculatePromotion(String tag, List<RiverRace> riverRaces, PlayerInOut inOut) {

        List<RiverRaceParticipant> oldWayParticipations = riverRaces.stream()
                .filter(riverRace -> riverRace.getSeasonId() != 0 && riverRace.getSeasonId() < 73)
                .map(RiverRace::getClan)
                .flatMap(c -> c.getParticipants().stream())
                .filter(p -> p.getTag().equals(tag))
                .collect(Collectors.toList());


        List<RiverRaceParticipant> newWayParticipations = riverRaces.stream()
                .filter(riverRace -> riverRace.getSeasonId() == 0 || riverRace.getSeasonId() >= 73)
                .map(RiverRace::getClan)
                .flatMap(c -> c.getParticipants().stream())
                .filter(p -> p.getTag().equals(tag))
                .collect(Collectors.toList());

        if (oldWayParticipations.isEmpty() && newWayParticipations.isEmpty()) {
            log.debug("no participation found for player {}", tag);
            return Collections.emptyList();
        }

        if (inOut == null) {
            log.debug("no checking / out  found for player {}", tag);
            return Collections.emptyList();
        }


        List<Integer> points = newWayParticipations.stream().map(rrp -> calculateParticipationScore(rrp, inOut)).collect(Collectors.toList());

        if (!oldWayParticipations.isEmpty()) {
            removeNegativeScoreConditionally(inOut, oldWayParticipations, riverRaces, true);
        }
        points.addAll(
                oldWayParticipations.stream().map(RiverRaceParticipant::getScore).map(PromotionDiff::getPromotionPoint).collect(Collectors.toList())
        );

        List<Integer> aggregatedPoints = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            List<Integer> subParticipation = new ArrayList<>(points.subList(i, points.size()));
            aggregatedPoints.add(subParticipation.stream().reduce(0, Integer::sum));
        }


        return aggregatedPoints;
    }

    public List<Integer> calculatePromotion(String tag) {

        List<RiverRace> riverRaces = riverRaceRepository.getRiverRaces(PageRequest.of(0, 12)).stream()
                .filter(riverRace -> riverRace.getClan().getParticipants().stream().anyMatch(p -> p.getTag().equals(tag)))
                .collect(Collectors.toList());

        PlayerInOut inOut = checkInService.findByTag(tag).orElse(null);

        return doCalculatePromotion(tag, riverRaces, inOut);
    }

    /**
     * new way of calculationg participation score
     */
    private int calculateParticipationScore(RiverRaceParticipant rrp, PlayerInOut inOut) {
        int avgScorePerGame = 131;
        int secondChanceAvgScore = 162; //expected score per game to get at most 0 if not played the required decks
        if (rrp.getRequiredDecks() == rrp.getWarDecks()) {
            if (rrp.getScore() > avgScorePerGame * rrp.getRequiredDecks()) { //2100
                return 2;
            } else {
                return 1;
            }
        } else if ((float) rrp.getWarDecks() / (float) rrp.getRequiredDecks() >= 14f / 16f) {
            if (rrp.getScore() > secondChanceAvgScore * rrp.getRequiredDecks()) { //2160
                return 1;
            } else {
                return 0;
            }
        } else if ((float) rrp.getWarDecks() / (float) rrp.getRequiredDecks() >= 12f / 16f) {
            if (rrp.getScore() > 112.5 * rrp.getRequiredDecks()) {//1800 (half wins at 12)
                return -1;
            }
        }
        return -2;

    }


    public List<PlayerPromotionDto> calculatePromotions() {
        log.debug("calculatePromotions");
        List<RiverRace> riverRaces = new ArrayList<>(riverRaceRepository.getRiverRaces(PageRequest.of(0, 12)));

        Map<String, List<RiverRaceParticipant>> participationByTag = riverRaces.stream()
                .map(RiverRace::getClan)
                .flatMap(c -> c.getParticipants().stream())
                .collect(Collectors.groupingBy(RiverRaceParticipant::getTag));


        if (participationByTag.isEmpty()) {
            log.debug("calculatePromotions list is empty - return");
            return Collections.emptyList();
        }

        Map<String, PlayerInOut> inOut = checkInService.findInClan().stream().collect(Collectors.toMap(PlayerInOut::getTag, i -> i));

        Map<String, Player> inClanPlayers = playerRepository.findInClan().stream().collect(Collectors.toMap(Player::getTag, p -> p));

        //filter by players in clan
        participationByTag.keySet().stream().filter(p -> !inClanPlayers.containsKey(p))
                .collect(Collectors.toList())
                .forEach(participationByTag::remove);


        Map<String, List<Integer>> promPointsByTag = participationByTag.keySet().stream()
                .collect(Collectors.toMap(k -> k, tag -> doCalculatePromotion(tag, riverRaces, inOut.get(tag))));

        return participationByTag.keySet().stream()
                .filter(tag -> !promPointsByTag.get(tag).isEmpty())
                .map(tag -> buildPlayerPromortionDto(participationByTag.get(tag), inOut.get(tag), inClanPlayers.get(tag), promPointsByTag.get(tag)))
                .collect(Collectors.toList());
    }

    private PlayerPromotionDto buildPlayerPromortionDto(List<RiverRaceParticipant> participation, PlayerInOut inOut, Player player, List<Integer> promPoints) {

        ArrayList<RiverRaceParticipant> excludeActiveParticipationList = new ArrayList<>(participation);

        RiverRaceParticipant activeParticipant = excludeActiveParticipationList.remove(0);
        PlayerPromotionDto playerPromotionDto = PlayerPromotionDto.builder()
                .tag(player.getTag())
                .name(player.getName())
                .role(player.getRole())
                .promotionPoints(promPoints.size() > 1 ? promPoints.get(1) : 0)
                .daysInClanAtEndOfRace(ChronoUnit.DAYS.between(inOut.getCheckIn(), LocalDateTime.now()))
                .totalPromotionPoints(promPoints.get(0))
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
