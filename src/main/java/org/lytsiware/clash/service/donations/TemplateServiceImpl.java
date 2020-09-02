package org.lytsiware.clash.service.donations;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TemplateServiceImpl implements TemplateService {

    private final PlayerWeeklyStatsRepository playerWeeklyStatsRepository;

    @Autowired
    public TemplateServiceImpl(PlayerWeeklyStatsRepository playerWeeklyStatsRepository) {
        this.playerWeeklyStatsRepository = playerWeeklyStatsRepository;
    }

    @Override
    public String generateTemplate() {
        StringBuilder sb = new StringBuilder();
        Week week = Week.now();
        List<PlayerWeeklyStats> result = playerWeeklyStatsRepository.findByWeek(week);
        if (result.isEmpty()) {
            week = week.minusWeeks(1);
            result = playerWeeklyStatsRepository.findByWeek(week);
        }
        if (result.isEmpty()) {
            week = week.minusWeeks(2);
            result = playerWeeklyStatsRepository.findByWeek(week);
        }
        week = week.plusWeeks(1);
        sb.append("WEEK=").append(week.getWeek()).append(" //this week is from monday ").append(week.getStartDate())
                .append("\r\n");
        sb.append("TAG, NAME, RANK, DONATIONS, CHEST_CONTR").append("\r\n");
        result.forEach(p -> sb.append(p.getPlayer().getTag()).append(",") //
                .append(p.getPlayer().getName()).append(",") //
                .append(p.getPlayer().getRole()).append(",") //
                .append(p.getCardDonation()).append(",") //
                .append(p.getChestContribution()) //
                .append("\r\n"));
        return sb.toString();

    }
}
