package org.lytsiware.clash.controller;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.aggregation.PlayerAggregationWarStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.dto.ClansWarGlobalStatsDto;
import org.lytsiware.clash.service.war.PlayerAggregationWarStatsService;
import org.lytsiware.clash.service.war.PlayerWarStatsService;
import org.lytsiware.clash.service.war.WarUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/rest")
public class WarStatsRestController {


    @Autowired
    WarUploadService warUploadService;

    @Autowired
    PlayerAggregationWarStatsService playerAggregationWarStatsService;

    @PostMapping(value = "/tagFile")
    public void tagFile(@RequestParam("file") MultipartFile file, Model model, HttpServletResponse response) throws IOException {
        log.info("START tagFile");
        String replaced = warUploadService.replaceNameWithTag(file.getInputStream(), file.getOriginalFilename());
        response.getOutputStream().write(replaced.getBytes(StandardCharsets.UTF_8));
        response.setContentType("application/text");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getOriginalFilename() + "\"");
        response.flushBuffer();

    }


    @PostMapping("/uploadWarStats")
    @Transactional(propagation = Propagation.REQUIRED)
    public void uploadWarStats(@RequestParam("file") MultipartFile[] files, Model model) throws IOException {
        log.info("START uploadWarStats");
        for (MultipartFile file : files) {
            warUploadService.upload(file.getInputStream(), file.getOriginalFilename());
        }
    }

    @GetMapping("/calculateWarStats")
    public void calculateWarStats(){
        log.info("START calculating missing war stats");
        playerAggregationWarStatsService.calculateMissingStats(null, null);
    }


    @GetMapping("/warStats/{deltaWeek}")
    public ClansWarGlobalStatsDto getWarStatsForWeeks(@PathVariable(value = "deltaWeek", required = false) Integer deltaWeek) {
        log.info("START warStats deltaWeek = {}", deltaWeek);

        List<PlayerAggregationWarStats> playerAggregationWarStats = playerAggregationWarStatsService.findLatestWarAggregationStatsForWeek(Week.now().minusWeeks(deltaWeek));

        return new ClansWarGlobalStatsDto(playerAggregationWarStats);

    }


}
