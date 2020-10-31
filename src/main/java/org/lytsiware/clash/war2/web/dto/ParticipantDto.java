package org.lytsiware.clash.war2.web.dto;

import lombok.Data;

@Data
public class ParticipantDto {
    String tag;
    String name;
    int activeFame;
    int fame;
    int repairPoints;
    int score;
    int promotionPoints;

    public int getScore() {
        return activeFame + repairPoints;
    }
}
