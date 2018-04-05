package org.lytsiware.clash.service.clan;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.clanweeklystats.ClanWeeklyStatRepository;
import org.lytsiware.clash.domain.clanweeklystats.ClanWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.lytsiware.clash.dto.ClanWeeklyStatsDto;
import org.lytsiware.clash.service.ClanChestScoreService;
import org.lytsiware.clash.service.calculation.CalculationContext;
import org.lytsiware.clash.service.calculation.chestscore.ClanChestScoreCalculationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ClanChestScoreServiceImpl implements ClanChestScoreService {

    private PlayerWeeklyStatsRepository playerWeeklyStatsRepository;
    private ClanChestScoreCalculationService clanChestScoreCalculationService;
    private ClanWeeklyStatRepository clanWeeklyStatRepository;

    @Autowired
    public ClanChestScoreServiceImpl(PlayerWeeklyStatsRepository playerWeeklyStatsRepository,
                                     ClanChestScoreCalculationService clanChestScoreCalculationService,
                                     ClanWeeklyStatRepository clanWeeklyStatRepository) {
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
        this.clanChestScoreCalculationService = clanChestScoreCalculationService;
        this.clanWeeklyStatRepository = clanWeeklyStatRepository;
    }

    @Override
    @Transactional(value = Transactional.TxType.REQUIRED)
    public void calculateAndUpdateClanChestScore(Week week) {
        List<PlayerWeeklyStats> playerWeeklyStats = playerWeeklyStatsRepository.findByWeek(week);
        if (playerWeeklyStats.size() == 0) {
            return;
        }
        CalculationContext context = clanChestScoreCalculationService.calculateChestScore(playerWeeklyStats);

        double score = context.get(CalculationContext.FINAL_DEVIATION, Double.class);
        double playerDeviationSoore = context.get(CalculationContext.PLAYER_DEVIATION_PERC, Double.class);
        double crownScore = context.get(CalculationContext.CROWN_SCORE_PERC, Double.class);

        ClanWeeklyStats clanWeeklyStat = clanWeeklyStatRepository.findOne(week.getWeek());
        if (clanWeeklyStat == null) {
            clanWeeklyStat = new ClanWeeklyStats();
            clanWeeklyStat.setWeek(week.getWeek());
        }
        clanWeeklyStat.setClanChestScore(score);
        clanWeeklyStat.setCrownScore(crownScore);
        clanWeeklyStat.setPlayerDeviationScore(playerDeviationSoore);
        clanWeeklyStatRepository.save(clanWeeklyStat);
    }

    @Override
    public List<ClanWeeklyStatsDto> getClanChestScore(Week from, Week to) {
        List<ClanWeeklyStats> clanStats = clanWeeklyStatRepository.findByWeekBetweenOrderByWeekAsc(from.getWeek(), to.getWeek());
        List<ClanWeeklyStatsDto> clanStatsDto = clanStats.stream().map(ClanWeeklyStatsDto::from).collect(Collectors.toList());
        for (ClanWeeklyStatsDto clanWeeklyStatsDto : clanStatsDto) {
            List<Integer> data = playerWeeklyStatsRepository.findByWeek(Week.fromWeek(clanWeeklyStatsDto.getWeek())).stream()
                    .map(PlayerWeeklyStats::getChestContribution)
                    .filter(Objects::nonNull)
                    .sorted()
                    .collect(Collectors.toList());
            clanWeeklyStatsDto.setData(data);
        }
        return clanStatsDto;

    }
}
