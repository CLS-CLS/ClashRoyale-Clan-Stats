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
@NamedEntityGraph(name = "RiverRace.eager", subgraphs = @NamedSubgraph(name = "participant", attributeNodes = @NamedAttributeNode(value = "participants")),
        attributeNodes = {@NamedAttributeNode(value = "clan", subgraph = "participant")}
)
@SequenceGenerator(name = "RiverRaceSequence", sequenceName = "RIVER_RACE_SEQUENCE")
@Table(uniqueConstraints = @UniqueConstraint(name = "river_race_UC", columnNames = {"sectionIndex", "seasonId"}))
public class RiverRace {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "RiverRaceSequence")
    private Long id;

    private int sectionIndex;
    private int seasonId;
    private LocalDateTime createdOn;
    private LocalDateTime superCellCreatedDate;

    @Builder.Default
    private boolean active = true;

    private LocalDateTime updatedOn;
    private boolean finished;

    @OneToOne(cascade = CascadeType.ALL)
    private RiverRaceClan clan;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "river_race_fk")
    @OrderBy("tag")
    private List<RiverRaceClan> clans;

}
