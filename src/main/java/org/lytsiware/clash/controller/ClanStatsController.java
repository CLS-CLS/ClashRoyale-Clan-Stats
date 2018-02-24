package org.lytsiware.clash.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ClanStatsController {

    private Logger logger = LoggerFactory.getLogger(ClanStatsController.class);


    @GetMapping("/newplayers")
    public String newPlayersPageDefault(Model model) {
        logger.info("new players request");
        return "/index";
    }

    @GetMapping("/newplayers/{week:\\d*}")
    public String newPlayersPage(@PathVariable Integer week, Model model) {
        logger.info("new players request for week {}", week);
        return "/index";
    }

    @GetMapping("/upload")
    public String uploadPage(Model model) {
        logger.info("upload request");
        return "/index";
    }

    @GetMapping("/clanrules")
    public String clanRules(){
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

}
