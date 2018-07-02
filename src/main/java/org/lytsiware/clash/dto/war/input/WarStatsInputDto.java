package org.lytsiware.clash.dto.war.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.validation.FieldsSumUpTo;
import org.lytsiware.clash.validation.ZeroToThree;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarStatsInputDto implements Serializable {

    @NotNull
    private String leagueName;

    @NotNull
    private LocalDate startDate;

    @Min(1)
    @Max(5)
    private int rank;

    private int trophies;

    @Builder.Default
    @Valid
    private List<PlayerWarStatInputDto> playerWarStats = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<PlayerWarStatInputDto> playersNotParticipated = new ArrayList<>();


    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Data
    @FieldsSumUpTo(fieldNames = {"gamesWon", "gamesLost", "gamesNotPlayed"}, sumFieldName = "gamesGranted")
    public static class PlayerWarStatInputDto {

        @Builder.Default
        private boolean delete = false;

        private String name;

        @NotNull
        private String tag;

        @ZeroToThree
        private Integer gamesGranted;

        @ZeroToThree
        private Integer gamesWon;

        @ZeroToThree
        private Integer gamesLost;

        @ZeroToThree
        private Integer gamesNotPlayed;

        @PositiveOrZero
        private Integer cards;

        public PlayerWarStatInputDto(PlayerWarStat warStat, boolean delete) {
            this.delete = delete;
            this.name = warStat.getPlayer().getName();
            this.tag = warStat.getPlayer().getTag();
            this.gamesGranted = warStat.getWarPhaseStats().getGamesGranted();
            this.gamesWon = warStat.getWarPhaseStats().getGamesWon();
            this.gamesLost = warStat.getWarPhaseStats().getGamesLost();
            this.gamesNotPlayed = warStat.getWarPhaseStats().getGamesNotPlayed();
            this.cards = warStat.getCollectionPhaseStats().getCardsWon();
        }

        public static PlayerWarStatInputDto zeroFieldPlayerWarStatInputDto(String tag, String name) {
            return PlayerWarStatInputDto.builder().tag(tag).name(name).gamesNotPlayed(0).gamesLost(0).gamesGranted(0).gamesWon(0).cards(0).build();
        }
    }

}
