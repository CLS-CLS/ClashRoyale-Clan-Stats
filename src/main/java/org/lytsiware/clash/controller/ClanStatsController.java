package org.lytsiware.clash.controller;

import java.time.LocalDate;
import java.util.List;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clan")
public class ClanStatsController {

    Logger logger = LoggerFactory.getLogger(ClanStatsController.class);

    @Autowired
    private ClanStatsService clanStatsService;


    @RequestMapping(value = "/{deltaWeek}", method = RequestMethod.GET)
    public List<PlayerOverallStats> retrieveClanStats(@PathVariable(required = false) Integer deltaWeek) {
        logger.info("START retrieveClanStats - week {}", deltaWeek);

        if (deltaWeek < 1 || deltaWeek > 12) {
           deltaWeek = 1;
        }
        Week week = new Week((LocalDate.now().minusWeeks(deltaWeek)));
        return clanStatsService.retrieveClanStats(week);

    }

}
