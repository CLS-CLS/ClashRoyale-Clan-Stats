package org.lytsiware.clash.service.integration;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Profile("clashStats")
public class ClashStatsSiteServiceImpl implements SiteIntegrationService {
	
	Logger logger = LoggerFactory.getLogger(ClashStatsSiteServiceImpl.class);
	
	@Autowired
	SiteConfigurationService siteConfigurationService;


	@Override
	public List<PlayerWeeklyStats> retrieveData() {
		Document document = this.createDocumentFromResource(siteConfigurationService.getDataResource());
		Elements subresult = document.select("#tbl");
		Elements subresult2 = subresult.select(".memberRow");
		List<PlayerWeeklyStats> playerWeeklyStats = new ArrayList<>();

		for (Element el : subresult2) {
			Elements memberLink = el.select("a");
			String memberUrl = memberLink.attr("href");
			String memberTag = memberUrl.split("Player/")[1];
			String memberName = memberLink.text();
			String[] chestContrArray = el.select(".lineC").text().split(",");
			int chestContribution = Integer.parseInt(chestContrArray[chestContrArray.length - 1]);
			String[] cardDonationArray = el.select(".lineD").text().split(",");
			int cardDonation = Integer.parseInt(cardDonationArray[cardDonationArray.length - 1]);
			String role = el.select("div").get(2).text();
            Player player = new Player(memberTag, memberName, role, true);
            PlayerWeeklyStats stats = new PlayerWeeklyStats(player, Week.now().previous().getWeek(),
                    chestContribution, cardDonation, 0 , 0);
			playerWeeklyStats.add(stats);
		}

		return playerWeeklyStats;
	}



}
