package org.lytsiware.clash.controller;

import org.lytsiware.clash.Week;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClanStatsController {

    private Logger logger = LoggerFactory.getLogger(ClanStatsController.class);

    @GetMapping("/warstats/input")
    public String warStatsInput() {
        return "/index";
    }


    @GetMapping("/newplayers")
    public String newPlayersPageDefault(Model model) {
        logger.info("new players request");
        return "/index";
    }

    @GetMapping("/newplayers/{deltaWeek:\\d*}")
    public String newPlayersPage(@PathVariable Integer deltaWeek, Model model) {
        logger.info("new players request for week {}", Week.now().minusWeeks(deltaWeek));
        return "/index";
    }

    @GetMapping(value = "/warstats/{deltaWeek:\\d*}")
    public String getWarStats(@PathVariable Integer deltaWeek) {
        logger.info("getWarStats until week {}", Week.now().minusWeeks(deltaWeek));
        return "/index";
    }

    @GetMapping(value = "/warstats")
    public String getWarStats() {
        logger.info("getWarStats week 0");
        return "/index";
    }

    @GetMapping(value = "/newPlayers")
    public String getNewPlayersBetweenWeeks(@RequestParam Integer deltaFrom, @RequestParam(required = false, defaultValue = "0") Integer deltaTo) {
        if (deltaTo <= deltaFrom) {
            throw new IllegalArgumentException("'from' week should be before 'to' week");
        }
        return "/index";
    }


    @GetMapping("/upload")
    public String uploadPage(Model model) {
        logger.info("upload request");
        return "/index";
    }

    @GetMapping("/clanrules")
    public String clanRules() {
        logger.info("clan rules");
        return "/index";
    }

    @GetMapping("/")
    public String initialPage(Model model) {
        logger.info("main page request");
        return "/index";
    }

    @GetMapping("/{week:\\d*}")
    public String initialPageWithDigit(@PathVariable int week, Model model) {
        logger.info("stats request for week {}", week);
        return "/index";
    }

    @GetMapping("/clan/score")
    public String clanScore() {
        return "/index";
    }

    @GetMapping("/player/{tag}")
    public String memberPage(@PathVariable String tag, Model model) {
        logger.info("stats requested for tag {}", tag);
        return "/index";
    }

    @GetMapping("/player/{tag}/war")
    public String playerWarStats(@PathVariable String tag, Model model) {
        logger.info("war stats requested for tag {}", tag);
        return "/index";
    }

    @GetMapping("/scheduler")
    public String schedulers() {
        return "/scheduler";
    }

}
