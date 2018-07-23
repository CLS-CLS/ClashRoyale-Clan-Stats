package org.lytsiware.clash.domain.player;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

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

    private LocalDate checkIn;

    private LocalDate checkOut;

    public PlayerInOut(String tag, LocalDate checkIn) {
        this.tag = tag;
        this.checkIn = checkIn;
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
