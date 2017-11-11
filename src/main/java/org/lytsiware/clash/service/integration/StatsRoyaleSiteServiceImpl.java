package org.lytsiware.clash.service.integration;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.lytsiware.clash.utils.SiteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Profile("statsRoyale")
public class StatsRoyaleSiteServiceImpl implements SiteIntegrationService {

	Logger logger = LoggerFactory.getLogger(ClashStatsSiteServiceImpl.class);

	@Value("${clientDataUrl}")
	Resource dataResource;
	
	@Value("${clientDataRefreshUrl}")
	String refreshUrl;

	@Override
	public List<PlayerWeeklyStats> retrieveData() {
		
		if (!StringUtils.isEmpty(refreshUrl)) {
			logger.warn("Refresh url is not provided");
			refresh();
			try {
				Thread.sleep(5 * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Document document = SiteUtils.retrieveData(dataResource);
		Elements rowContainer = document.select(".clan__rowContainer");

		List<PlayerWeeklyStats> playerWeeklyStats = new ArrayList<>();

		for (Element el : rowContainer) {
			Elements memberLink = el.select(".ui__blueLink");
			String memberUrl = memberLink.attr("href");
			String memberTag = memberUrl.split("/profile/")[1];
			String memberName = memberLink.text();
			int chestContribution = Integer.valueOf(el.attr("data-crowns"));
			int cardDonation = Integer.valueOf(el.attr("data-donations"));
			String role = el.select(".clan__row").get(7).text().trim();
			Player player = new Player(memberTag, memberName, role);
			PlayerWeeklyStats stats = new PlayerWeeklyStats(player, new Week().minusWeeks(1).getWeek(),
					chestContribution, cardDonation, 0, 0);
			playerWeeklyStats.add(stats);
		}

		return playerWeeklyStats;
			
	}
	
	private void refresh() {
		try {
			
			URL url = new URL("https://statsroyale.com/clan/L88VPG/refresh");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
			int responseCode = con.getResponseCode();
			System.out.println(responseCode);
		} catch(Exception ex){
			throw new RuntimeException(ex);
		}
	}

}
