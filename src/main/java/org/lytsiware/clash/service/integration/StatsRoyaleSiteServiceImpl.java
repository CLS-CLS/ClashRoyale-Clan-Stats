package org.lytsiware.clash.service.integration;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Service
@Profile("statsRoyale")
@SiteQualifier(SiteQualifier.Name.STATS_ROYALE)
public class StatsRoyaleSiteServiceImpl implements RefreshableSiteIntegrationService {

	Logger logger = LoggerFactory.getLogger(ClashStatsSiteServiceImpl.class);

    private SiteConfigurationService siteConfigurationService;

    public StatsRoyaleSiteServiceImpl(SiteConfigurationService siteConfigurationService) {
        this.siteConfigurationService = siteConfigurationService;
    }

    @Override
	public List<PlayerWeeklyStats> retrieveData() {
		return retrieveData(true);
	}

	@Override
    public List<PlayerWeeklyStats> retrieveData(boolean requestRefresh) {
        return retrieveData(requestRefresh, siteConfigurationService.getDataResource(), siteConfigurationService.getRefreshUrl());
    }

    public List<PlayerWeeklyStats> retrieveData(boolean requestRefresh, Resource siteUrl, String refreshUrl) {
        logger.info("retrieveDate, requestRefresh: {}", requestRefresh);
        if (requestRefresh) {
            if (!StringUtils.isEmpty(refreshUrl)) {
                refresh();
                try {
                    Thread.sleep(5 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                logger.warn("Refresh url is not provided");
            }
        }
        Document document = this.createDocumentFromResource(siteUrl);
        Elements rowContainer = document.select(".clan__rowContainer");

        List<PlayerWeeklyStats> playerWeeklyStats = new ArrayList<>();

        for (Element el : rowContainer) {
            Elements memberLink = el.select(".ui__blueLink");
            String memberUrl = memberLink.attr("href");
            String memberTag = memberUrl.split("/profile/")[1];
            String memberName = memberLink.text();
//            int chestContribution = Integer.valueOf(el.attr("data-crowns"));
            int cardDonation = Integer.valueOf(el.attr("data-donations"));
            String role = el.select(".clan__memberRoleInner").text().trim();
            Player player = new Player(memberTag, memberName, role, true);
            PlayerWeeklyStats stats = new PlayerWeeklyStats(player, null, cardDonation, 0, 0);
            playerWeeklyStats.add(stats);
        }

        return playerWeeklyStats;

    }


	private void refresh() {
		try {
			URL url = new URL(siteConfigurationService.getRefreshUrl());
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
			logger.info("Refreshed with response code : " + con.getResponseCode());
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

}
