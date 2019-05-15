package org.lytsiware.clash.service.integration.clashapi;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CurrentWarDto {

    private static final String DATE_FORMAT = "yyyyMMdd'T'HHmmss.SSS'Z'"; //"20190511T152835.402Z"
    @NotNull
    private State state;
    @JsonFormat(pattern = DATE_FORMAT)
    private LocalDateTime warEndTime;
    @JsonFormat(pattern = DATE_FORMAT)
    private LocalDateTime collectionEndTime;
    private List<Participant> participants;

    public LocalDateTime getEndDate() {
        switch (state) {
            case WAR_DAY:
                return warEndTime;
            case COLLECTION_DAY:
                return collectionEndTime;
        }
        throw new IllegalStateException("state not found");
    }

    public enum State {
        @JsonProperty("warDay")
        WAR_DAY,

        @JsonProperty("collectionDay")
        COLLECTION_DAY,

        @JsonProperty("notInWar")
        NOT_IN_WAR
    }

    @Data
    public static class Participant {
        String tag;
        String name;
        Integer cardsEarned;
        Integer battlesPlayed;
        Integer wins;
        Integer collectionDayBattlesPlayed;
    }

}
