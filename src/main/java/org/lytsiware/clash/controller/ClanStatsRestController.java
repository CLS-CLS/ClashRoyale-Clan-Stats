package org.lytsiware.clash.controller;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.dto.NewPlayersDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.lytsiware.clash.service.calculation.oldsite.chestscore.ClanChestScoreCalculationService;
import org.lytsiware.clash.service.donations.DonationAggregationService;
import org.lytsiware.clash.service.donations.DonationStatsServiceImpl;
import org.lytsiware.clash.service.donations.PlayerCheckInService;
import org.lytsiware.clash.service.donations.TemplateService;
import org.lytsiware.clash.service.integration.RefreshableSiteIntegrationService;
import org.lytsiware.clash.service.integration.SiteQualifier;
import org.lytsiware.clash.service.war.integration.clashapi.ClashRoyaleRestIntegrationService;
import org.lytsiware.clash.service.war.job.ClashRoyaleWarJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/rest")
@Slf4j
public class ClanStatsRestController {

    @Autowired
    ClashRoyaleRestIntegrationService clashRoyaleRestIngegrationService;

    @Autowired
    ClashRoyaleWarJob clashRoyaleWarJob;

    @RequestMapping("/fixie")
    public void fixie() {
        clashRoyaleWarJob.run();
    }

    @Autowired
    private DonationStatsServiceImpl clanStatsService;

    @Autowired
    private DonationAggregationService donationAggregationService;

    @Autowired
    private ClanChestScoreCalculationService clanChestScoreCalculationService;

    @Autowired
    @SiteQualifier(SiteQualifier.Name.STATS_ROYALE)
    private RefreshableSiteIntegrationService siteIntegrationService;

    @Autowired
    private TemplateService templateService;

    @Autowired
    private PlayerCheckInService playerCheckInService;

    @RequestMapping(value = "/{deltaWeek}", method = RequestMethod.GET)
    public List<PlayerOverallStats> retrieveClanStats(@PathVariable(required = false) Integer deltaWeek) {
        log.info("Controller: retrieveClanStats - week {}", deltaWeek);

        if (deltaWeek < Constants.MIN_PAST_WEEK || deltaWeek > Constants.MAX_PAST_WEEK) {
            deltaWeek = Constants.DEFAULT_DELTA_WEEK;
        }
        Week week = Week.now().minusWeeks(deltaWeek);
        return clanStatsService.retrieveClanStats(week);

    }

    @RequestMapping(value = "/player/{tag}", method = RequestMethod.GET)
    public PlayerStatsDto retrievePlayerStats(@PathVariable String tag) {
        log.info("Controller: retrievePlayerStats - tag {}", tag);

        return clanStatsService.retrievePlayerStats(tag, Week.now().minusWeeks(Constants.MAX_PAST_WEEK + 1), Week.now());
    }

    @GetMapping(value = "/generateTemplate")
    public void generateTemplate(HttpServletResponse response) throws IOException {
        log.info("Controller: generateTemplate");
        response.getOutputStream().write(
                templateService.generateTemplate().getBytes(StandardCharsets.UTF_8));
        response.setContentType("application/text");
        response.setHeader("Content-Disposition", "attachment; filename=\"template.txt\"");
        response.flushBuffer();

    }

    @GetMapping(value = "/newPlayers/{deltaWeek}")
    public NewPlayersDto getNewPlayers(@PathVariable(required = false) Integer deltaWeek) {
        log.info("Controller: retrieveClanStats - week {}", deltaWeek);
        if (deltaWeek == null) {
            deltaWeek = 0;
        }

        Week week = Week.now().minusWeeks(deltaWeek);
        return clanStatsService.findNewPlayersOfWeeks(week.previous(), week);
    }

    @GetMapping(value = "/newPlayers")
    public NewPlayersDto getNewPlayersBetweenWeeks(@RequestParam Integer deltaFrom,
                                                   @RequestParam(required = false, defaultValue = "0") Integer deltaTo) {
        log.info("Controller: getNewPlayersBetweenWeeks");
        if (deltaTo >= deltaFrom) {
            throw new IllegalArgumentException("'from' week should be before 'to' week");
        }

        return clanStatsService.findNewPlayersOfWeeks(Week.now().minusWeeks(deltaFrom), Week.now().minusWeeks(deltaTo));
    }

    @GetMapping(value = "/info/week")
    public Integer getWeekNumber() {
        log.info("Controller: getWeekNumber");
        return Week.now().getWeek();
    }


    @GetMapping("/roster")
    public List<PlayerCheckInService.CheckInCheckoutDataDto> roster() {
        log.info("Controller: CheckInCheckoutDataDto");
        return playerCheckInService.getCheckinCheckoutData();
    }


}
