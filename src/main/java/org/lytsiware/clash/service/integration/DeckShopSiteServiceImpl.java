package org.lytsiware.clash.service.integration;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("statsRoyale")
@SiteQualifier(SiteQualifier.Name.DECK_SHOP)
public class DeckShopSiteServiceImpl implements SiteIntegrationService {

    Logger logger = LoggerFactory.getLogger(DeckShopSiteServiceImpl.class);

    @Autowired
    SiteConfigurationService siteConfigurationService;

    @Override
    public List<PlayerWeeklyStats> retrieveData() {
        return retrieveData(siteConfigurationService.getDeckshopClanUrlResource());
    }

    public List<PlayerWeeklyStats> retrieveData(Resource siteUrl) {
        logger.info("retrieveData from deckshop, requestRefresh:");

        Document document = this.createDocumentFromResource(siteUrl);
        Elements rowContainer = document.select(".clan__rowContainer");

        List<Element> tables = document.select("table").stream()
                .filter(e -> e.select("th").stream()
                        .anyMatch(se -> "Clan Chest".equals(se.text())))
                .collect(Collectors.toList());

        if (tables.size() != 1) {
            throw new ParseException("More than one table for stats was found");
        }

        Elements tableHeaders = tables.get(0).select("th");
        //check correctnes of indexes
        boolean ok = true;
        String reason = "Rank";
        ok = ok && tableHeaders.get(0).select("span").get(1).text().equals("Rank");
        if (ok) {
            ok = tableHeaders.get(1).text().equals("Arena");
            reason = "Arena";
        }
        if (ok) {
            ok = tableHeaders.get(2).text().trim().equals("Player Tag");
            reason = "player";
        }

        if (ok) {
            ok = tableHeaders.get(4).getElementsContainingText("Trophies").size() != 0;

            reason = "Trophies";
        }
        if (ok) {
            ok = tableHeaders.get(4).getElementsContainingText("Contribution").size() != 0;
            reason = "Contribution";
        }
        if (ok) {
            ok = tableHeaders.get(5).text().equals("Donations Received");
            reason = "Donations";
        }

        if (ok) {
            ok = ok && tableHeaders.get(6).text().equals("Clan Chest");
            reason = "Clan chest";
        }

        if (!ok) {
            throw new ParseException("Not ok because of " + reason);
        }

        List<PlayerWeeklyStats> playerWeeklyStats = new ArrayList<>();
        try {
            Elements players = tables.get(0).select("tbody").select("tr");
            for (Element player : players) {
                String tag = player.attr("id");
                logger.info("TAG {}", tag);
                Elements playerStats = player.select("td");
                Elements roleElements = playerStats.get(2).select("span");
                String role = roleElements.size() == 0 ? "Member" : roleElements.get(0).text();
                String name = playerStats.get(2).select("a").text();
                Integer contribution = Integer.valueOf(playerStats.get(5).select("span").get(0).text());
                Integer requests = Integer.valueOf(playerStats.get(5).select("span").get(1).text());
                String donationString = playerStats.get(6).select("span").text();
                Integer donations = StringUtils.isEmpty(donationString) ? 0 : Integer.valueOf(donationString);
                PlayerWeeklyStats playerWeeklyStat = PlayerWeeklyStats.builder()
                        .withPlayer(new Player(tag, name, role))
                        .withChestContribution(contribution)
                        .withCardDonation(donations)
                        .withCardsReceived(requests)
                        .build();

                playerWeeklyStats.add(playerWeeklyStat);
            }
        } catch (Exception ex) {
            throw new ParseException(ex);
        }

        return playerWeeklyStats;
    }

    public static void main(String[] args) throws MalformedURLException {
        DeckShopSiteServiceImpl shopSiteService = new DeckShopSiteServiceImpl();
        List<PlayerWeeklyStats> playerWeeklyStats = shopSiteService.retrieveData(
                new FileSystemResource("A:\\EcliseWorkspace\\ClashRoyale-Clan-Stats\\src\\test\\resources\\deckshop.html"));

    }

}
