package org.lytsiware.clash.donation.controller;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.core.service.job.scheduledname.ScheduledNameService;
import org.lytsiware.clash.donation.dto.NewPlayersUpdateDto;
import org.lytsiware.clash.donation.dto.PlayerOverallStats;
import org.lytsiware.clash.donation.service.DonationAggregationService;
import org.lytsiware.clash.donation.service.DonationStatsService;
import org.lytsiware.clash.donation.service.UpdateStatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest")
@Slf4j
public class ClanStatsSecuredRestController {

    @Autowired
    DonationStatsService donationStatsService;

    @Autowired
    ScheduledNameService scheduledNameService;

    @Autowired
    private DonationAggregationService donationAggregationService;

    @Autowired
    private UpdateStatService updateService;


    @PostMapping(value = "/newPlayers/update/{deltaWeek}")
    public List<PlayerOverallStats> keepOrDiscardNewPlayerStats(@PathVariable(required = false) Integer deltaWeek,
                                                                @RequestBody List<NewPlayersUpdateDto> updateDto) {
        log.info("Controller: keepOrDiscardNewPlayerStats");
        if (deltaWeek == null) {
            deltaWeek = 0;
        }
        Week week = Week.now().minusWeeks(deltaWeek);
        return donationStatsService.resetStatsOfNewPlayers(week, updateDto);
    }

    @GetMapping("/scheduler/{name}")
    public void runScheduler(@PathVariable String name) {
        log.info("Controller: runScheduler");
        scheduledNameService.runScheduler(name);
    }

    @GetMapping("/scheduler")
    public List<Map<String, String>> getRegisteredSchedulers() {
        log.info("Controller: getRegisteredSchedulers");
        return scheduledNameService.getScheduledInfo();
    }


}
