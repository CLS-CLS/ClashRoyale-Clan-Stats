package org.lytsiware.clash.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.ClanStatsServiceImpl;
import org.lytsiware.clash.service.integration.StatsRoyaleSiteServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {


	@Autowired
	ClanStatsServiceImpl clanStatService;

	@Autowired
	StatsRoyaleSiteServiceImpl siteService;

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
	

	//@RequestMapping("/recalculate-avg/{week}")
	public String recalculateAvgs(@PathVariable(value = "week") Integer week) {
		if (week < 1) {
			return "/index/";
		}
		clanStatService.recalculateAndSaveAvgs(Week.fromWeek(week));
		return "redirect:/" + (Week.now().getWeek() - week);
	}

	//@RequestMapping("/updateDonations/{week}")
	public String runSchedulerUpdateOrInsertDonations(@PathVariable(value = "week") Integer week) {
		if (week == 0) {
			week = Week.now().getWeek();
		}
		List<PlayerWeeklyStats> stats = siteService.retrieveData(true);
		clanStatService.updateOrInsertNewDonationsAndRole(stats, Week.fromWeek(week), true);
		return "/index/" + (Week.now().getWeek() - week);
	}

}
