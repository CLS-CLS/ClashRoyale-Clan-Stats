package org.lytsiware.clash.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClanStatsController {
	
	@Value("${base.url}") String baseUrl;
	
	@GetMapping("/")
	public String initialPage(Model model) {
		model.addAttribute("baseUrl", baseUrl);
		return "/index";
	}

}
