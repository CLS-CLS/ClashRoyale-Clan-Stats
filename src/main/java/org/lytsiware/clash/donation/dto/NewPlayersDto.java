package org.lytsiware.clash.donation.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NewPlayersDto implements Serializable {

    private final LocalDate oldWeek;
    private final LocalDate newWeek;

    List<PlayerOverallStats> newPlayers = new ArrayList<>();

    public NewPlayersDto(LocalDate oldWeek, LocalDate newWeek, List<PlayerOverallStats> newPlayers) {
        this.oldWeek = oldWeek;
        this.newWeek = newWeek;
        this.newPlayers = newPlayers;
    }

    public LocalDate getOldWeek() {
        return oldWeek;
    }

    public LocalDate getNewWeek() {
        return newWeek;
    }

    public List<PlayerOverallStats> getNewPlayers() {
        return newPlayers;
    }
}
