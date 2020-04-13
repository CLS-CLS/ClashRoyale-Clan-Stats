package org.lytsiware.clash.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.integration.statsroyale.StatsRoyaleSiteServiceImpl;
import org.lytsiware.clash.service.war.WarConstants;
import org.lytsiware.clash.service.war.WarInputService;
import org.lytsiware.clash.service.war.WarUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@Slf4j
public class AdminController {

    @Autowired
    WarInputService warInputService;
    @Autowired
    private StatsRoyaleSiteServiceImpl siteService;
    @Autowired
    private WarUploadService warUploadService;
    @Autowired
    private AggregationService aggregationService;

    @GetMapping("rest/warstats/addNotParticipated")
    public ResponseEntity<Void> addNotParticipated(@RequestParam @DateTimeFormat(pattern = "dd-MM-yyyy") LocalDate leagueDate,
                                                   @RequestParam(defaultValue = "" + WarConstants.leagueSpan) int leagueSpan) {
        warInputService.addNotParticipated(leagueDate, leagueSpan);
        return ResponseEntity.ok().build();

    }


    @PostMapping("/uploadwarstats")
    @Transactional(propagation = Propagation.REQUIRED)
    public String uploadWarStats(@RequestParam("file") MultipartFile[] files, Model model) throws IOException {
        log.info("Controller: uploadWarStats");
        warUploadService.upload(files);
        return "redirect:/warstats/0";
    }


    //@RequestMapping("/check")
    public String checkParsing(@RequestParam("refresh") Boolean refresh, Model model) throws JsonProcessingException {
        log.info("Controller: checkParsing");
        refresh = (refresh != null ? refresh : false);
        List<PlayerWeeklyStats> clanStats = siteService.retrieveData(refresh);
        ObjectMapper objectMapper = new ObjectMapper();
        String clanstatsAsJson = objectMapper.writeValueAsString(clanStats);
        model.addAttribute("stats", clanstatsAsJson);
        return "/check";
    }

    @RequestMapping("/safecheck")
    public String checkParsingNoRefresh(Model model) throws JsonProcessingException {
        log.info("Controller: checkParsingNoRefresh");
        //TODO create check page
        //TODO add security
        return checkParsing(false, model);

    }


    @RequestMapping("/calculateAvg/{deltaWeek}")
    @Transactional
    public String recalculateAvgs(@PathVariable(value = "deltaWeek") Integer deltaWeek) {
        log.info("Controller: recalculateAvgs");
        if (deltaWeek < 0 || deltaWeek > 6) {
            return "/index/";
        }
        Week week = Week.now().minusWeeks(deltaWeek);
        aggregationService.calculateAndSaveAvgs(week);
        return "redirect:/" + deltaWeek;
    }


}
