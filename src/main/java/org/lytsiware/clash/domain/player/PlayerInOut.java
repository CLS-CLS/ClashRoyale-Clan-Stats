package org.lytsiware.clash.domain.player;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@AllArgsConstructor
@Builder
@Data
@SequenceGenerator(name = "PLAYER_IN_OUT_SEQUENCE", sequenceName = "PLAYER_IN_OUT_SEQUENCE", allocationSize = 1)
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"TAG"}))
public class PlayerInOut {


    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "PLAYER_IN_OUT_SEQUENCE")
    private Long id;

    private String tag;

    private LocalDateTime checkIn;

    private LocalDateTime checkOut;

    public PlayerInOut(String tag, LocalDateTime checkIn) {
        this.tag = tag;
        this.checkIn = checkIn;
    }

    public PlayerInOut(String tag, LocalDateTime checkIn, LocalDateTime checkOut) {
        this.tag = tag;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        PlayerInOut that = (PlayerInOut) o;

        return tag.equals(that.tag);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + tag.hashCode();
        return result;
    }
}
