package org.lytsiware.clash.controller;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.dto.NewPlayersUpdateDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.service.ClanStatsService;
import org.lytsiware.clash.service.job.ScheduledNameService;
import org.lytsiware.clash.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class ClanStatsSecuredRestController {

    private static final Logger logger = LoggerFactory.getLogger(ClanStatsSecuredRestController.class);

    @Autowired
    ClanStatsService clanStatsService;

    //    @Autowired
    ScheduledNameService scheduledNameService;

//    @GetMapping("/clanchestscore/{deltaWeek}")
    public void calculateAndSaveClanchestScore (@PathVariable("deltaWeek") Integer deltaWeek) {
        clanStatsService.calculateAndUpdateClanChestScore(Week.now().minusWeeks(deltaWeek));
    }

//   @PostMapping(value="/newPlayers/update/{deltaWeek}")
    public List<PlayerOverallStats> keepOrDiscardNewPlayerStats(@PathVariable(required = false) Integer deltaWeek, @RequestBody List<NewPlayersUpdateDto> updateDto) {
        if (deltaWeek == null) {
            deltaWeek = 0;
        }
        Week week = Week.now().minusWeeks(deltaWeek);
        return clanStatsService.resetStatsOfNewPlayers(week, updateDto);
    }

    @GetMapping("/scheduler/{name}")
    public void runScheduler(String name) {
        scheduledNameService.runScheduler(name);
    }

    @GetMapping("/scheduler")
    public List<String> getRegisteredSchedulers() {
        return scheduledNameService.getScheduledNames();
    }

//  @PostMapping("/upload")
    public void upload(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        logger.info("upload request");
        String content = new String(file.getBytes(), Charset.forName("UTF-8"));
        String[] lines = content.split("\r\n");
        List<PlayerWeeklyStats> statsList = new ArrayList<>();
        Week week = Week.now().previous();
        for (int i = 2; i < lines.length; i++ ){
            String[] stats = lines[i].split(",");
            String tag = stats[0].trim();
            String name = stats[1].trim();
            String rank = stats[2].trim();
            String donations = stats[3].trim();
            String cc = stats[4].trim();
            Player player = new Player(tag, name , rank);

            PlayerWeeklyStats pws = new PlayerWeeklyStats(player, week.getWeek() ,
                    Utils.parseNullableInt(cc), Utils.parseNullableInt(donations),0,0);

            statsList.add(pws);
        }

        clanStatsService.updateOrInsertNewDonationsAndRole(statsList, week, true);
    }
}
