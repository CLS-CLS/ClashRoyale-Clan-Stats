package org.lytsiware.clash.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClashStatsSiteService  implements IClashSiteService {
	
	@Value("${clientDataUrl}")
	Resource dataResource;
	
		
	@Override
	public List<PlayerWeeklyStats> retrieveData() {
		//Document result = Jsoup.parse(new URL("https://clashstat.com/Home/Clan/802LU8UY"), 10000);
		
		Document result;
		try {
			result = Jsoup.parse(dataResource.getFile(), "utf-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Elements subresult = result.select("#tbl");
		Elements subresult2 = subresult.select(".memberRow");
		List<PlayerWeeklyStats> playerWeeklyStats = new ArrayList<>();
		for (Element el : subresult2) {
			Elements memberLink = el.select("a");
			String memberUrl = memberLink.attr("href");
			String memberTag = memberUrl.split("Member=")[1];
			String memberName = memberLink.text();
			int chestContribution = Integer.parseInt(el.select(".lineC").text().split(",")[1]);
			int cardDonation = Integer.parseInt(el.select(".lineD").text().split(",")[1]);

			Player player = new Player(memberTag, memberName, 0, 0);
			PlayerWeeklyStats stats = new PlayerWeeklyStats(player, new Week().minusWeeks(1).getWeek(),chestContribution, cardDonation);
			playerWeeklyStats.add(stats);
		}
		
		return playerWeeklyStats;

	}

}
