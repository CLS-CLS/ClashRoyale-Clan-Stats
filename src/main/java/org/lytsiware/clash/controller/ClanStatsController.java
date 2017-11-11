package org.lytsiware.clash.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class ClanStatsController {
	private Logger logger = LoggerFactory.getLogger(ClanStatsController.class);
	@Value("${base.url}") String baseUrl;
	
	@GetMapping("/upload")
	public String uploadPage(Model model){
		logger.info("upload request");
		model.addAttribute("baseUrl", baseUrl);
		return "/index";
	}
	
	@GetMapping("/")
	public String initialPage(Model model) {
		logger.info("main page request");
		model.addAttribute("baseUrl", baseUrl);
		return "/index";
	}
	
	@GetMapping("/{week:\\d*}")
	public String initialPageWithDigit(@PathVariable int week, Model model) {
		logger.info("stats request for week {}", week);
		model.addAttribute("baseUrl", baseUrl);
		return "/index";
	}
	
	
	@GetMapping("/player/{tag}")
	public String memberPage(@PathVariable String tag, Model model) {
		logger.info("stats requested for tag {}", tag);
		model.addAttribute("baseUrl", baseUrl);
		return "/index";
	}

}
