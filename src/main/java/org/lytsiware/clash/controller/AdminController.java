package org.lytsiware.clash.controller;

import java.util.List;

import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.integration.ClashStatsSiteService;
import org.lytsiware.clash.service.job.ClanStatsJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Value("${base.url}")
	String baseUrl;

	@Autowired
	ClanStatsService clanStatService;

	@Autowired
	ClashStatsSiteService siteService;

	@Autowired
	ClanStatsJob job;

	@RequestMapping("/check")
	public String checkParsing(Model model) throws JsonProcessingException {
		//TODO create check page
		//TODO add security
		//TODO create session to allow to import these stats now
		List<PlayerWeeklyStats> clanStats = siteService.retrieveData();
		model.addAttribute("baseUrl" , baseUrl);
		ObjectMapper objectMapper = new ObjectMapper();
		String clanstatsAsJson = objectMapper.writeValueAsString(clanStats);
		model.addAttribute("stats", clanstatsAsJson);
		return "/check";
	}

	//@RequestMapping("/enforce-run")
	public String enfornceRun() {
		//	TODO disabled until there is security implemented
		job.run();
		return "/index";
	}
	
	//@RequestMapping("/recalculate-avg")
	public String recalculateAvgs()	{
		//	TODO disabled until there is security implemented
		clanStatService.recalculateAvgs();
		return "/index";
	}

}
