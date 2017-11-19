package org.lytsiware.clash.controller;

import java.util.List;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.ClanStatsServiceImpl;
import org.lytsiware.clash.service.integration.StatsRoyaleSiteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Value("${base.url}")
	String baseUrl;

	@Autowired
	ClanStatsServiceImpl clanStatService;

	@Autowired
	StatsRoyaleSiteServiceImpl siteService;

	//@RequestMapping("/check")
	public String checkParsing(@RequestParam("refresh") Boolean refresh,  Model model) throws JsonProcessingException {
		//TODO create check page
		//TODO add security
		//TODO create session to allow to import these stats now
		refresh = (refresh != null? refresh : false);
		List<PlayerWeeklyStats> clanStats = siteService.retrieveData(refresh);
		model.addAttribute("baseUrl" , baseUrl);
		ObjectMapper objectMapper = new ObjectMapper();
		String clanstatsAsJson = objectMapper.writeValueAsString(clanStats);
		model.addAttribute("stats", clanstatsAsJson);
		return "/check";
	}
	
	@RequestMapping("/safecheck")
	public String checkParsingNoRefresh(Model model) throws JsonProcessingException {
		//TODO create check page
		//TODO add security
		//TODO create session to allow to import these stats now
		return checkParsing(false, model);
		
	}
	

	// @RequestMapping("/recalculate-avg/{week}")
	public String recalculateAvgs(@PathVariable(value = "week") Integer week) {
		if (week < 1) {
			return "/index/";
		}
		clanStatService.recalculateAvgs(new Week(week));
		return "redirect:/" + (new Week().getWeek() - week);
	}

	// @RequestMapping("/update/{week}")
	public String runSchedulerUpdateOrInsertDonations(@PathVariable(value = "week") Integer week) {
		List<PlayerWeeklyStats> stats = siteService.retrieveData();
		clanStatService.updateOrInsertNewDonations(stats, new Week(week), true);
		return "/index/" + (new Week().getWeek() - week);
	}

}
