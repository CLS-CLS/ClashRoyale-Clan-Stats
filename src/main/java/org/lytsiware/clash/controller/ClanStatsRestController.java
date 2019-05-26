package org.lytsiware.clash.controller;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.dto.ClanWeeklyStatsDto;
import org.lytsiware.clash.dto.NewPlayersDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.TemplateService;
import org.lytsiware.clash.service.calculation.chestscore.ClanChestScoreCalculationService;
import org.lytsiware.clash.service.clan.ClanStatsServiceImpl;
import org.lytsiware.clash.service.integration.RefreshableSiteIntegrationService;
import org.lytsiware.clash.service.integration.SiteQualifier;
import org.lytsiware.clash.service.integration.clashapi.ClashRoyaleRestIntegrationService;
import org.lytsiware.clash.service.job.ClashRoyaleWarJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class ClanStatsRestController {

    private static final Logger logger = LoggerFactory.getLogger(ClanStatsRestController.class);

    @Autowired
    ClashRoyaleRestIntegrationService clashRoyaleRestIngegrationService;

    @Autowired
    ClashRoyaleWarJob clashRoyaleWarJob;

    @RequestMapping("/fixie")
    public void fixie() {
        clashRoyaleWarJob.run();
    }

    @Autowired
    private ClanStatsServiceImpl clanStatsService;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private ClanChestScoreCalculationService clanChestScoreCalculationService;

    @Autowired
    @SiteQualifier(SiteQualifier.Name.STATS_ROYALE)
    private RefreshableSiteIntegrationService siteIntegrationService;

    @Autowired
    private TemplateService templateService;

    @RequestMapping(value = "/{deltaWeek}", method = RequestMethod.GET)
    public List<PlayerOverallStats> retrieveClanStats(@PathVariable(required = false) Integer deltaWeek) {
        logger.info("START retrieveClanStats - week {}", deltaWeek);

        if (deltaWeek < Constants.MIN_PAST_WEEK || deltaWeek > Constants.MAX_PAST_WEEK) {
            deltaWeek = Constants.DEFAULT_DELTA_WEEK;
        }
        Week week = Week.now().minusWeeks(deltaWeek);
        return clanStatsService.retrieveClanStats(week);

    }

    @RequestMapping(value = "/player/{tag}", method = RequestMethod.GET)
    public PlayerStatsDto retrievePlayerStats(@PathVariable String tag) {
        logger.info("START retrievePlayerStats - tag {}", tag);

        return clanStatsService.retrievePlayerStats(tag, Week.now().minusWeeks(Constants.MAX_PAST_WEEK + 1), Week.now());
    }

    @GetMapping(value = "/generateTemplate")
    public void generateTemplate(HttpServletResponse response) throws IOException {
        logger.info("START generateTemplate");
        response.getOutputStream().write(
                templateService.generateTemplate().getBytes(Charset.forName("UTF-8")));
        response.setContentType("application/text");
        response.setHeader("Content-Disposition", "attachment; filename=\"template.txt\"");
        response.flushBuffer();

    }

    @GetMapping(value = "/newPlayers/{deltaWeek}")
    public NewPlayersDto getNewPlayers(@PathVariable(required = false) Integer deltaWeek) {
        if (deltaWeek == null) {
            deltaWeek = 0;
        }

        Week week = Week.now().minusWeeks(deltaWeek);
        return clanStatsService.findNewPlayersOfWeeks(week.previous(), week);
    }

    @GetMapping(value = "/newPlayers")
    public NewPlayersDto getNewPlayersBetweenWeeks(@RequestParam Integer deltaFrom, @RequestParam(required = false, defaultValue = "0") Integer deltaTo) {
        if (deltaTo >= deltaFrom) {
            throw new IllegalArgumentException("'from' week should be before 'to' week");
        }

        return clanStatsService.findNewPlayersOfWeeks(Week.now().minusWeeks(deltaFrom), Week.now().minusWeeks(deltaTo));
    }

    @GetMapping(value = "/info/week")
    public Integer getWeekNumber() {
        return Week.now().getWeek();
    }

    @GetMapping(value = "/clan/score")
    public List<ClanWeeklyStatsDto> getClanChestScore() {
        return aggregationService.getClanChestScore(Week.now().minusWeeks(24), Week.now().previous());
    }

}
