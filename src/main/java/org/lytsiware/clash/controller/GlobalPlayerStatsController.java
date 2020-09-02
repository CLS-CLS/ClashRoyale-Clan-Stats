package org.lytsiware.clash.controller;


import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.GlobalPlayerStat;
import org.lytsiware.clash.service.gobalstats.GlobalStatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/rest")
@Slf4j
public class GlobalPlayerStatsController {

    @Autowired
    private GlobalStatsService globalStatsService;

    @GetMapping("/globalStats")
    public List<GlobalPlayerStat> globalPlayerStats() {
        return globalStatsService.globalPlayerStats();
    }
}
