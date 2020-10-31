package org.lytsiware.clash.war2.service;

import lombok.Getter;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.domain.player.PlayerInOut;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.domain.RiverRaceClan;
import org.lytsiware.clash.war2.domain.RiverRaceParticipant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class PromotionData {
    private static final LocalDateTime NOW = LocalDateTime.of(2020, 10, 17, 17, 13, 0); //saturday

    private final List<PlayerInOut> playersInOut = Stream.of(
            new PlayerInOut("tag1", NOW.minusDays(8), null, false),
            new PlayerInOut("tag2", NOW.minusDays(16), null, false),
            new PlayerInOut("tag3", LocalDateTime.of(2020, 10, 8, 15, 0), null, false)
    ).collect(Collectors.toList());

    private final List<Player> players = Stream.of(
            new Player("tag1", "tag1", "member"),
            new Player("tag2", "tag1", "member"),
            new Player("tag3", "tag1", "member")

    ).collect(Collectors.toList());

    List<RiverRace> riverRaces = Stream.of(
            RiverRace.builder()
                    .active(true)
                    .clan(RiverRaceClan.builder().participants(Stream.of(
                            RiverRaceParticipant.builder().tag("tag1").activeFame(10).build(),
                            RiverRaceParticipant.builder().tag("tag2").activeFame(10).build(),
                            RiverRaceParticipant.builder().tag("tag3").activeFame(10).build()
                    ).collect(Collectors.toList())).build()).build(),
            RiverRace.builder()
                    .active(false)
                    .superCellCreatedDate(NOW)
                    .clan(RiverRaceClan.builder().participants(Stream.of(
                            RiverRaceParticipant.builder().tag("tag1").activeFame(10).build(),
                            RiverRaceParticipant.builder().tag("tag2").activeFame(10).build(),
                            RiverRaceParticipant.builder().tag("tag3").activeFame(10).build()
                    ).collect(Collectors.toList())).build()).build(),
            RiverRace.builder()
                    .active(false)
                    .superCellCreatedDate(NOW.minusDays(7))
                    .clan(RiverRaceClan.builder().participants(Stream.of(
                            RiverRaceParticipant.builder().tag("tag1").activeFame(10).build(),
                            RiverRaceParticipant.builder().tag("tag2").activeFame(10).build(),
                            RiverRaceParticipant.builder().tag("tag3").activeFame(10).build()
                    ).collect(Collectors.toList())).build()).build()
    ).collect(Collectors.toList());

    List<RiverRace> riverRacesAlt = Stream.of(
            RiverRace.builder()
                    .active(true)
                    .clan(RiverRaceClan.builder().participants(Stream.of(
                            RiverRaceParticipant.builder().tag("tag3").activeFame(0).build()
                    ).collect(Collectors.toList())).build()).build(),
            RiverRace.builder()
                    .active(false)
                    .superCellCreatedDate(LocalDateTime.of(2020, 10, 12, 9, 37))
                    .clan(RiverRaceClan.builder().participants(Stream.of(
                            RiverRaceParticipant.builder().tag("tag3").activeFame(608).build()
                    ).collect(Collectors.toList())).build()).build()

    ).collect(Collectors.toList());
}
