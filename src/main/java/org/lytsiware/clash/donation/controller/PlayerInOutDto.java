package org.lytsiware.clash.donation.controller;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class PlayerInOutDto {

    String name;
    String tag;
    List<InOutDto> inAndOuts;


    @Data
    @Builder
    public static class InOutDto {
        LocalDateTime joinedAt;
        LocalDateTime leftAt;
    }

}
