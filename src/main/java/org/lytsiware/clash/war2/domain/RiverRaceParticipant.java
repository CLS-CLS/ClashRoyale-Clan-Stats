package org.lytsiware.clash.war2.domain;

import lombok.*;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "RiverRaceParticipantSequence", sequenceName = "RIVER_RACE_PARTICIPANT_SEQUENCE")
public class RiverRaceParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RiverRaceParticipantSequence")
    private Long id;
    private String tag;
    private String name;
    private int fame;
    private int repairPoints;
    private int activeFame;

    public int getScore() {
        return activeFame + repairPoints;
    }


}
