package org.lytsiware.clash.service.integration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class ClashStatsSiteServiceImpl implements ClashStatsSiteService {
	
	Logger logger = LoggerFactory.getLogger(ClashStatsSiteServiceImpl.class);
	
	@Value("${clientDataUrl}")
	Resource dataResource;

	@Override
	public List<PlayerWeeklyStats> retrieveData() {
		logger.info("Rertrieving data from {}", dataResource);
		// Document result = Jsoup.parse(new
		// URL("https://clashstat.com/Home/Clan/802LU8UY"), 10000);
		
		try {
			URL url = dataResource.getURL();
			URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			String inputLine;
			while ((inputLine = br.readLine()) != null) {
				sb.append(inputLine).append("\n");
			}
			br.close();
			Document result = Jsoup.parse(sb.toString(), "utf-8");
			return parseHtml(result);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<PlayerWeeklyStats> parseHtml(Document result) {
		Elements subresult = result.select("#tbl");
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
			Player player = new Player(memberTag, memberName, role);
			PlayerWeeklyStats stats = new PlayerWeeklyStats(player, new Week().minusWeeks(1).getWeek(),
					chestContribution, cardDonation, 0 , 0);
			playerWeeklyStats.add(stats);
		}

		return playerWeeklyStats;
	}

}
