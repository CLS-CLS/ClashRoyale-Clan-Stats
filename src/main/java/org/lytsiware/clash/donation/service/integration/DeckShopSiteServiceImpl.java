package org.lytsiware.clash.donation.service.integration;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.service.integration.ParseException;
import org.lytsiware.clash.core.service.integration.SiteConfigurationService;
import org.lytsiware.clash.core.service.integration.SiteIntegrationService;
import org.lytsiware.clash.core.service.integration.SiteQualifier;
import org.lytsiware.clash.donation.domain.PlayerWeeklyStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Profile("statsRoyale")
@SiteQualifier(SiteQualifier.Name.DECK_SHOP)
public class DeckShopSiteServiceImpl implements SiteIntegrationService<List<PlayerWeeklyStats>> {

    private final Logger logger = LoggerFactory.getLogger(DeckShopSiteServiceImpl.class);

    private final SiteConfigurationService siteConfigurationService;

    @Autowired
    public DeckShopSiteServiceImpl(SiteConfigurationService siteConfigurationService) {
        this.siteConfigurationService = siteConfigurationService;
    }

    @Override
    public List<PlayerWeeklyStats> retrieveData() {
        return retrieveData(siteConfigurationService.getDeckshopClanUrlResource());
    }

    public List<PlayerWeeklyStats> retrieveData(Resource siteUrl) {
        logger.info("retrieveData from deckshop");

        Document document = this.createDocumentFromResource(siteUrl);

        Element table = findStatTable(document);

        List<PlayerWeeklyStats> playerWeeklyStats = new ArrayList<>();
        try {
            Elements players = table.select("tbody").select("tr");
            for (Element player : players) {
                String tag = player.attr("id");
                logger.info("Parsing element with tag: {}", tag);
                Elements playerStats = player.select("td");
                Elements roleElements = playerStats.get(2).select("span");
                String role = roleElements.size() == 0 ? "Member" : roleElements.get(0).text();
                String name = playerStats.get(2).select("a").text();
                Integer requests = Integer.valueOf(playerStats.get(5).select("span").get(1).text());
                String donationString = playerStats.get(5).select("span").get(0).text();
                Integer donations = StringUtils.isEmpty(donationString) ? 0 : Integer.valueOf(donationString);
                PlayerWeeklyStats playerWeeklyStat = PlayerWeeklyStats.builder()
                        .withPlayer(new Player(tag, name, role, true))
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

    private Element findStatTable(Document document) throws ParseException {
        List<Element> tables = document.select("table").stream()
                .filter(e -> e.select("th").stream().anyMatch(se -> "Donations Received".equals(se.text())))
                .collect(Collectors.toList());

        if (tables.size() != 1) {
            throw new ParseException("None or more than one table for stats was found");
        }

        Elements tableHeaders = tables.get(0).select("th");
        check("Rank", tableHeaders.get(0).select("span").get(1).text().equals("Rank"));
        check("Arena", tableHeaders.get(1).text().equals("Arena"));
        check("player", tableHeaders.get(2).text().trim().equals("Player Tag"));
        check("Trophies", tableHeaders.get(4).getElementsContainingText("Trophies").size() != 0);
//        commented out as there is no cups at the moment
//        check("Contribution", tableHeaders.get(4).getElementsContainingText("Cups").size() != 0);
        check("Donations", tableHeaders.get(5).text().equals("Donations Received"));
        return tables.get(0);
    }

    private void check(String reason, boolean ok) {
        if (!ok) {
            throw new ParseException("Not ok because of " + reason);
        }
    }

}
