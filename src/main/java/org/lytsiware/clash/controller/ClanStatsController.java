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

	@GetMapping("/")
	public String initialPage(Model model) {
		logger.info("main page request");
		return "enter";
	}

    @GetMapping("/{path}")
    public String init(@PathVariable String path) {
        logger.info("request for path {}", path);
        return "main";
    }

    @GetMapping("/player/**")
    public String player() {
        logger.info("request for player");
        return "main";
    }


    @GetMapping("/warstats/**")
    public String warstats() {
        logger.info("request for warstats input");
        return "main";
    }

	@GetMapping("/scheduler")
	public String scheduler() {
		logger.info("request for warstats input");
		return "main";
	}

	@GetMapping("/newplayers/**")
	public String newplayers() {
		logger.info("request for new players input");
		return "main";
	}



}
