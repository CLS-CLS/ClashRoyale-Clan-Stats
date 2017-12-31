package org.lytsiware.clash.controller;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.dto.ClanWeeklyStatsDto;
import org.lytsiware.clash.dto.NewPlayersDto;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.lytsiware.clash.service.ClanStatsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

@RestController
@RequestMapping("/rest")
public class ClanStatsRestController {

    private static final Logger logger = LoggerFactory.getLogger(ClanStatsRestController.class);

    @Autowired
    private ClanStatsServiceImpl clanStatsService;
    
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
    public NewPlayersDto getNewPlayers(@PathVariable(required = false) Integer deltaWeek) {
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
    	return clanStatsService.getClanChestScore(Week.now().minusWeeks(13), Week.now().previous());
	}
    
    

   
}
