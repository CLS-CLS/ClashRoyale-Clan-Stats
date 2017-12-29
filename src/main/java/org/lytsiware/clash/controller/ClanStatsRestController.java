package org.lytsiware.clash.controller;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.dto.ClanWeeklyStatsDto;
import org.lytsiware.clash.dto.NewPlayersUpdateDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.lytsiware.clash.service.ClanStatsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class ClanStatsRestController {

    Logger logger = LoggerFactory.getLogger(ClanStatsRestController.class);

    @Autowired
    private ClanStatsServiceImpl clanStatsService;
    
  //  @PostMapping("/upload")
	public void upload(@RequestParam("file") MultipartFile file, Model model) throws IOException{
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
			
			PlayerWeeklyStats pws = new PlayerWeeklyStats(player, week.getWeek() , parseNullableInt(cc), parseNullableInt(donations),0,0);
			statsList.add(pws);
		}
		clanStatsService.updateOrInsertNewDonations(statsList, week, true);
	}
    
    private Integer parseNullableInt(String integer){
    	try {
    		return Integer.parseInt(integer);
    	} catch (Exception ex){
    		return null;
    	}
    }

    @RequestMapping(value = "/{deltaWeek}", method = RequestMethod.GET)
    public List<PlayerOverallStats> retrieveClanStats(@PathVariable(required = false) Integer deltaWeek) {
        logger.info("START retrieveClanStats - week {}", deltaWeek);

        if (deltaWeek < 1 || deltaWeek > Constants.MAX_PAST_WEEK) {
           deltaWeek = 1;
        }
        Week week = Week.now().minusWeeks(deltaWeek);
        return clanStatsService.retrieveClanStats(week);

    }
    
    @RequestMapping(value="/player/{tag}", method= RequestMethod.GET)
    public PlayerStatsDto retrievePlayerStats(@PathVariable(required = true) String tag) {
    	logger.info("START retrievePlayerStats - tag {}", tag);
    	
    	return clanStatsService.retrievePlayerStats(tag, Week.now().minusWeeks(13), Week.now().minusWeeks(1) );
    }
    
    @GetMapping(value="/generateTemplate")
    public void generateTemplate(HttpServletResponse response) throws IOException{
    	logger.info("START generateTemplate");
    	response.getOutputStream().write(
    			clanStatsService.generateTemplate().getBytes(Charset.forName("UTF-8")));
    	response.setContentType("application/text");      
    	response.setHeader("Content-Disposition", "attachment; filename=\"template.txt\""); 
    	response.flushBuffer();
    
    }
    
    @GetMapping(value="/newPlayers/{deltaWeek}")
    public List<PlayerOverallStats> getNewPlayers(@PathVariable(required = false) Integer deltaWeek) {
    	if (deltaWeek == null) {
    		deltaWeek = 0;
    	}

    	Week week = Week.now().minusWeeks(deltaWeek);
    	return clanStatsService.findNewPlayersAtWeeks(week.previous(), week);
    }
    
    @GetMapping(value="/info/week")
    public Integer getWeekNumber() {
    	return Week.now().previous().getWeek();
    }

    @GetMapping(value="/clan/score")
	public List<ClanWeeklyStatsDto> getClanChestScore() {
    	return clanStatsService.getClanChestScore(Week.now().minusWeeks(5), Week.now().previous());
	}
    
    
//    @PostMapping(value="/newPlayers/update/{deltaWeek}")
    public List<PlayerOverallStats> keepOrDiscardNewPlayerStats(@PathVariable(required = false) Integer deltaWeek, @RequestBody List<NewPlayersUpdateDto> updateDto) {
    	if (deltaWeek == null) {
    		deltaWeek = 0;
    	}
    	Week week = Week.now().minusWeeks(deltaWeek);
    	return clanStatsService.resetStatsOfNewPlayers(week, updateDto);
    }
   
}
