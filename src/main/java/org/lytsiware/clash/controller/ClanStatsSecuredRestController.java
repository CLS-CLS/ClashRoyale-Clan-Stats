package org.lytsiware.clash.controller;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.dto.NewPlayersUpdateDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.service.AggregationService;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.UpdateStatService;
import org.lytsiware.clash.service.job.scheduledname.ScheduledNameService;
import org.lytsiware.clash.service.war.WarUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/rest")
@Slf4j
public class ClanStatsSecuredRestController {

    @Autowired
    ClanStatsService clanStatsService;

    @Autowired
    ScheduledNameService scheduledNameService;

    @Autowired
    private AggregationService aggregationService;

    @Autowired
    private UpdateStatService updateService;

    @Autowired
    private WarUploadService warUploadService;


    @PostMapping(value = "/newPlayers/update/{deltaWeek}")
    public List<PlayerOverallStats> keepOrDiscardNewPlayerStats(@PathVariable(required = false) Integer deltaWeek,
                                                                @RequestBody List<NewPlayersUpdateDto> updateDto) {
        log.info("Controller: keepOrDiscardNewPlayerStats");
        if (deltaWeek == null) {
            deltaWeek = 0;
        }
        Week week = Week.now().minusWeeks(deltaWeek);
        return clanStatsService.resetStatsOfNewPlayers(week, updateDto);
    }

    @GetMapping("/scheduler/{name}")
    public void runScheduler(@PathVariable String name) {
        log.info("Controller: runScheduler");
        scheduledNameService.runScheduler(name);
    }

    @GetMapping("/scheduler")
    public List<Map<String, String>> getRegisteredSchedulers() {
        log.info("Controller: getRegisteredSchedulers");
        return scheduledNameService.getScheduledInfo();
    }


    @PostMapping("/upload")
    public void uploadWarFile(@RequestParam("file") MultipartFile multipartFile, Model model) throws IOException {
        log.info("Controller: uploadWarFile");
        warUploadService.upload(new MultipartFile[]{multipartFile});
    }

}
