package org.lytsiware.clash.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.integration.statsroyale.StatsRoyaleSiteServiceImpl;
import org.lytsiware.clash.service.war.WarUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

	@Autowired
	StatsRoyaleSiteServiceImpl siteService;

	@Autowired
    WarUploadService warUploadService;

	@Autowired
    AggregationService aggregationService;


    @PostMapping("/uploadWarStats")
    @Transactional(propagation = Propagation.REQUIRED)
    public String uploadWarStats(@RequestParam("file") MultipartFile[] files, Model model) throws IOException {
        log.info("START uploadWarStats");
        warUploadService.upload(files);
        return "redirect:/warStats/0";
    }


    //@RequestMapping("/check")
	public String checkParsing(@RequestParam("refresh") Boolean refresh,  Model model) throws JsonProcessingException {
		refresh = (refresh != null? refresh : false);
		List<PlayerWeeklyStats> clanStats = siteService.retrieveData(refresh);
		ObjectMapper objectMapper = new ObjectMapper();
		String clanstatsAsJson = objectMapper.writeValueAsString(clanStats);
		model.addAttribute("stats", clanstatsAsJson);
		return "/check";
	}
	
	@RequestMapping("/safecheck")
	public String checkParsingNoRefresh(Model model) throws JsonProcessingException {
		//TODO create check page
		//TODO add security
		return checkParsing(false, model);
		
	}


    @RequestMapping("/calculateAvg/{deltaWeek}")
    @Transactional
    public String recalculateAvgs(@PathVariable(value = "deltaWeek") Integer deltaWeek) {
        if (deltaWeek < 0 || deltaWeek > 6) {
            return "/index/";
		}
        Week week = Week.now().minusWeeks(deltaWeek);
        aggregationService.calculateAndSaveAvgs(week);
        return "redirect:/" + deltaWeek;
    }


}
