package org.lytsiware.clash.war.dto.input;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lytsiware.clash.utils.validation.FieldsSumUpTo;
import org.lytsiware.clash.utils.validation.ZeroToThree;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStat;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import java.io.Serializable;
import java.time.LocalDateTime;
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
    private LocalDateTime startDate;

    @Min(1)
    @Max(5)
    private int rank;

    private int trophies;

    @Min(0)
    private int totalTrophies;

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

        @ZeroToThree
        private Integer collectionBattlesPlayed;

        public PlayerWarStatInputDto(PlayerWarStat warStat, boolean delete) {
            this.delete = delete;
            this.name = warStat.getPlayer().getName();
            this.tag = warStat.getPlayer().getTag();
            this.gamesGranted = warStat.getWarPhaseStats().getGamesGranted();
            this.gamesWon = warStat.getWarPhaseStats().getGamesWon();
            this.gamesLost = warStat.getWarPhaseStats().getGamesLost();
            this.gamesNotPlayed = warStat.getWarPhaseStats().getGamesNotPlayed();
            this.cards = warStat.getCollectionPhaseStats().getCardsWon();
            this.collectionBattlesPlayed = warStat.getCollectionPhaseStats().getGamesPlayed();
        }

        public static PlayerWarStatInputDto zeroFieldPlayerWarStatInputDto(String tag, String name) {
            return PlayerWarStatInputDto.builder().tag(tag).name(name).gamesNotPlayed(0).gamesLost(0).gamesGranted(0).gamesWon(0).cards(0).collectionBattlesPlayed(0).build();
        }
    }


}
