package org.lytsiware.clash.controller;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.service.ClanStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest/secure")
public class ClanStatsSecuredRestController {

    @Autowired
    ClanStatsService clanStatsService;

    @GetMapping("/clanchestscore/{deltaWeek}")
    public void calculateAndSaveClanchestScore (@PathVariable("deltaWeek") Integer deltaWeek) {
        clanStatsService.calculateAndUpdateClanChestScore(Week.now().minusWeeks(deltaWeek));
    }
}
