package org.lytsiware.clash.domain.player;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Builder
@Data
@SequenceGenerator(name = "PLAYER_IN_OUT_HIST_SEQUENCE", sequenceName = "PLAYER_IN_OUT_HIST_SEQUENCE", allocationSize = 1)
@Table
public class PlayerInOutHistory {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PLAYER_IN_OUT_HIST_SEQUENCE")
    private Long id;

    private String tag;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    private boolean abandonedWar;

    public static PlayerInOutHistory from(PlayerInOut playerInOut) {
        return PlayerInOutHistory.builder()
                .tag(playerInOut.getTag())
                .checkIn(playerInOut.getCheckIn())
                .checkOut(playerInOut.getCheckOut())
                .abandonedWar(playerInOut.hasAbandonedWar())
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlayerInOutHistory that = (PlayerInOutHistory) o;

        if (!tag.equals(that.tag)) return false;
        return checkIn.equals(that.checkIn);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + tag.hashCode();
        result = 31 * result + checkIn.hashCode();
        return result;
    }

    public boolean hasAbandonedWar() {
        return abandonedWar;
    }
}
