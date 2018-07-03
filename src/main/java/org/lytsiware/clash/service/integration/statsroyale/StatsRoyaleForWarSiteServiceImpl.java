package org.lytsiware.clash.service.integration.statsroyale;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.dto.war.input.WarStatsInputDto;
import org.lytsiware.clash.service.integration.ParseException;
import org.lytsiware.clash.service.integration.RefreshableSiteIntegrationService;
import org.lytsiware.clash.service.integration.SiteConfigurationService;
import org.lytsiware.clash.service.integration.SiteQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@Profile("statsRoyale")
@SiteQualifier(SiteQualifier.Name.STATS_ROYALE_WAR)
public class StatsRoyaleForWarSiteServiceImpl implements RefreshableSiteIntegrationService<List<WarStatsInputDto>> {

    Logger logger = LoggerFactory.getLogger(StatsRoyaleSiteServiceImpl.class);

    private SiteConfigurationService siteConfigurationService;

    public StatsRoyaleForWarSiteServiceImpl(SiteConfigurationService siteConfigurationService) {
        this.siteConfigurationService = siteConfigurationService;
    }

    public static void main(String[] args) {

        List<WarStatsInputDto> warLeagues = new StatsRoyaleForWarSiteServiceImpl(new SiteConfigurationService(null, null, null, "file:///c:/users/christos/desktop/warStats.htm")).retrieveData();

    }

    @Override
    public List<WarStatsInputDto> retrieveData() {
        return retrieveData(true);
    }

    @Override
    public List<WarStatsInputDto> retrieveData(boolean requestRefresh) {
        return retrieveData(requestRefresh, siteConfigurationService.getStatsRoyeleWarUrl(), siteConfigurationService.getRefreshUrl());
    }

    public List<WarStatsInputDto> retrieveData(boolean requestRefresh, String siteUrl, String refreshUrl) {
        logger.info("retrieveDate, siteUrl={} requestRefresh: {}", siteUrl, requestRefresh);
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


        try {
            Document document = this.createDocumentFromResource(new UrlResource(siteUrl));

            Elements pastClanWars = document.select(".clanWarHistory__season");

            List<WarStatsInputDto> warStatsInputDtos = new ArrayList<>();

            for (Element pastClanWar : pastClanWars) {

                String leagueFakeName = pastClanWar.select(".clanWarHistory__header").select(".clanWarHistory__createdTime").text();

                Element leagueGeneralStats = findClanDivWithTags(pastClanWar);

                String rankAsString = leagueGeneralStats.select(".clanWarHistory__row").get(0).text().substring(1);
                int rank = Integer.valueOf(rankAsString);
                String deltaString = leagueGeneralStats.select(".clanWarHistory__deltaUp").text();
                if (StringUtils.isEmpty(deltaString)) {
                    deltaString = leagueGeneralStats.select(".clanWarHistory__deltaDown").text();
                }
                int delta = Integer.valueOf(deltaString);

                Elements playerStats = pastClanWar.nextElementSibling().select(".clanParticipants__rowContainer");


                WarStatsInputDto warStatsInputDto = WarStatsInputDto.builder()
                        .leagueName(leagueFakeName)
                        .startDate(LocalDate.now())
                        .rank(rank)
                        .trophies(delta)
                        .build();
                warStatsInputDtos.add(warStatsInputDto);

                for (Element playerStat : playerStats) {
                    String tag = Arrays.stream(playerStat.select(".clanParticipants__row").get(1).select("a").attr("href").split("/"))
                            .reduce((first, second) -> second).orElseThrow(() -> new ParseException("Could not parse player's tag"));
                    String name = playerStat.select(".clanParticipants__row").get(1).select("a").text();

                    String battlesAsString = playerStat.select(".clanParticipants__row").stream()
                            .filter(element -> element.select(".clanParticipants__battleIcon").size() == 1)
                            .findFirst()
                            .orElseThrow(() -> new ParseException("Could not find battles for player" + name)).text();

                    String winsAsString = playerStat.select(".clanParticipants__row").stream()
                            .filter(element -> element.select(".clanParticipants__winIcon").size() == 1)
                            .findFirst()
                            .orElseThrow(() -> new ParseException("Could not find wins for player" + name)).text();

                    String cardsAsString = playerStat.select(".clanParticipants__row").stream()
                            .filter(element -> element.select(".clanParticipants__cardsIcon").size() == 1)
                            .findFirst()
                            .orElseThrow(() -> new ParseException("Could not find cards for player" + name)).text();

                    int battles = Integer.valueOf(battlesAsString);
                    int wins = Integer.valueOf(winsAsString);
                    int cards = Integer.valueOf(cardsAsString);

                    warStatsInputDto.getPlayerWarStats().add(
                            WarStatsInputDto.PlayerWarStatInputDto.builder()
                                    .name(name)
                                    .tag(tag)
                                    .cards(cards)
                                    .gamesGranted(battles)
                                    .gamesWon(wins)
                                    .gamesLost(battles - wins)
                                    .gamesNotPlayed(0).build());
                }
            }

            return warStatsInputDtos;

        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

    }

    private Element findClanDivWithTags(Element pastClanWar) {
        Elements clanDivs = pastClanWar.select(".clanWarHistory__cell");
        for (Element clanDiv : clanDivs) {
            String clanTag = Arrays.stream(clanDiv.select(".clanWarHistory__row")
                    .get(1).select("a").attr("href").split("/")).reduce((first, second) -> second).orElse("");
            if (clanTag.equals("20G0YGP")) { //  802LU8UY
                return clanDiv;
            }
        }
        throw new ParseException("Could not find the clan's div");
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
