package org.lytsiware.clash.war2.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "RiverRaceClanSequence", sequenceName = "RIVER_RACE_CLAN_SEQUENCE")
public class RiverRaceClan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RiverRaceClanSequence")
    private int id;

    @Column(nullable = false)
    private String tag;

    private String name;
    private int trophies;
    private int trophyChange;
    private int fame;
    private int repairPoints;
    private LocalDateTime finishTime;
    private int rank;
    private boolean finished;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "river_race_clan_fk")
    private List<RiverRaceParticipant> participants;
}
