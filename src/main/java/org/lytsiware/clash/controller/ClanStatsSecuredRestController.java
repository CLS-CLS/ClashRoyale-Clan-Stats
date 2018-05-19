package org.lytsiware.clash.controller;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.dto.NewPlayersUpdateDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.UpdateStatService;
import org.lytsiware.clash.service.job.scheduledname.ScheduledNameService;
import org.lytsiware.clash.service.war.PlayerWarStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest")
public class ClanStatsSecuredRestController {

    private static final Logger logger = LoggerFactory.getLogger(ClanStatsSecuredRestController.class);

    @Autowired
    ClanStatsService clanStatsService;

    @Autowired
    ScheduledNameService scheduledNameService;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private UpdateStatService updateService;

    @Autowired
    private PlayerWarStatsService playerWarStatsService;

    //    @GetMapping("/clanchestscore/{deltaWeek}")
    public void calculateAndSaveClanchestScore (@PathVariable("deltaWeek") Integer deltaWeek) {
        aggregationService.calculateAndUpdateClanChestScore(Week.now().minusWeeks(deltaWeek));
    }

    @PostMapping(value = "/newPlayers/update/{deltaWeek}")
    public List<PlayerOverallStats> keepOrDiscardNewPlayerStats(@PathVariable(required = false) Integer deltaWeek, @RequestBody List<NewPlayersUpdateDto> updateDto) {
        if (deltaWeek == null) {
            deltaWeek = 0;
        }
        Week week = Week.now().minusWeeks(deltaWeek);
        return clanStatsService.resetStatsOfNewPlayers(week, updateDto);
    }

    @GetMapping("/scheduler/{name}")
    public void runScheduler(@PathVariable String name) {
        scheduledNameService.runScheduler(name);
    }

    @GetMapping("/scheduler")
    public List<Map<String, String>> getRegisteredSchedulers() {
        return scheduledNameService.getScheduledNames();
    }


    @PostMapping("/upload")
    public void uploadWarFile(@RequestParam("file") MultipartFile multipartFile, Model model) throws IOException {
        logger.info("upload request");
        playerWarStatsService.upload(multipartFile.getInputStream(), multipartFile.getOriginalFilename());
    }

}
