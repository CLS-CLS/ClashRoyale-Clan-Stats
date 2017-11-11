package org.lytsiware.clash.controller;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.dto.PlayerOverallStats;
import org.lytsiware.clash.dto.PlayerStatsDto;
import org.lytsiware.clash.service.ClanStatsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
		Week week = new Week().minusWeeks(1);
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

        if (deltaWeek < 1 || deltaWeek > 12) {
           deltaWeek = 1;
        }
        Week week = new Week().minusWeeks(deltaWeek);
        return clanStatsService.retrieveClanStats(week);

    }
    
    @RequestMapping(value="/player/{tag}", method= RequestMethod.GET)
    public PlayerStatsDto retrievePlayerStats(@PathVariable(required = true) String tag) {
    	logger.info("START retrievePlayerStats - tag {}", tag);
    	
    	return clanStatsService.retrievePlayerStats(tag);
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

}
