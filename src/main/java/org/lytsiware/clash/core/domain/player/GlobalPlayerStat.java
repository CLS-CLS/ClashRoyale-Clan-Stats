package org.lytsiware.clash.core.domain.player;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Immutable
@Getter
@Setter
@Table(name="GLOBAL_PLAYER_STATS")
public class GlobalPlayerStat {

    @Id
    String tag;

    String name;

    String role;

    Boolean inClan;

    Integer eligibleGames;

    Integer playedWarGames;

    Double participationRatio;

    Integer abandonedWarGames;

    Integer abandonedCollectionGames;

    Double abandonedWarGamesRatio;

    Double abandonedCollectionGamesRatio;
}
