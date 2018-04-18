package org.lytsiware.clash.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NewPlayersDto implements Serializable {

    private LocalDate oldWeek;
    private LocalDate newWeek;

    List<NewPlayerOverallStats> newPlayers = new ArrayList<>();

    public NewPlayersDto(LocalDate oldWeek, LocalDate newWeek, List<NewPlayerOverallStats> newPlayers) {
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

    public List<NewPlayerOverallStats> getNewPlayers() {
        return newPlayers;
    }
}
