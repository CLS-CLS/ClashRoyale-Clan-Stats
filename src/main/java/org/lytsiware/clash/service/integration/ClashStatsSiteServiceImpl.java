package org.lytsiware.clash.service.integration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.player.PlayerWeeklyStats;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

@Service
public class ClashStatsSiteServiceImpl implements ClashStatsSiteService {
	
	@Value("${clientDataUrl}")
	Resource dataResource;
	
		
	@Override
	public List<PlayerWeeklyStats> retrieveData() {
//		Document result = Jsoup.parse(new URL("https://clashstat.com/Home/Clan/802LU8UY"), 10000);
        try {
            URL url = dataResource.getURL();
            URLConnection connection = url.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36");
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
            String memberTag = memberUrl.split("Member=")[1];
            String memberName = memberLink.text();
            int chestContribution = Integer.parseInt(el.select(".lineC").text().split(",")[1]);
            int cardDonation = Integer.parseInt(el.select(".lineD").text().split(",")[1]);
            String role = el.select("div").get(2).text();
            Player player = new Player(memberTag, memberName, role, 0, 0);
            PlayerWeeklyStats stats = new PlayerWeeklyStats(player, new Week().minusWeeks(1).getWeek(),chestContribution, cardDonation);
            playerWeeklyStats.add(stats);
        }

        return playerWeeklyStats;
    }

}
