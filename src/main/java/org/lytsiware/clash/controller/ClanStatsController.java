package org.lytsiware.clash.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClanStatsController {

    private final Logger logger = LoggerFactory.getLogger(ClanStatsController.class);


    @GetMapping({"/", "/view/**"})
    public String init() {
        return "main";
    }

}
