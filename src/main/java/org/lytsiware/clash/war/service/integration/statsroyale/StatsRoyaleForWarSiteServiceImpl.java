package org.lytsiware.clash.war.service.integration.statsroyale;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.lytsiware.clash.core.service.integration.ParseException;
import org.lytsiware.clash.core.service.integration.RefreshableSiteIntegrationService;
import org.lytsiware.clash.core.service.integration.SiteConfigurationService;
import org.lytsiware.clash.core.service.integration.SiteQualifier;
import org.lytsiware.clash.donation.service.integration.StatsRoyaleSiteServiceImpl;
import org.lytsiware.clash.war.dto.input.WarStatsInputDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Profile("statsRoyale")
@SiteQualifier(SiteQualifier.Name.STATS_ROYALE_WAR)
@RequiredArgsConstructor
public class StatsRoyaleForWarSiteServiceImpl implements RefreshableSiteIntegrationService<List<WarStatsInputDto>> {

    Logger logger = LoggerFactory.getLogger(StatsRoyaleSiteServiceImpl.class);

    @Value("${clanTag:PG2000PL}")
    private String clanTag;

    private final ObjectMapper objectMapper;

    private final SiteConfigurationService siteConfigurationService;

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
                int totalTrophies = Integer.valueOf(leagueGeneralStats.select(".clanWarHistory__trophies").text());

                int delta = Integer.valueOf(deltaString);

                Elements playerStats = pastClanWar.nextElementSibling().select(".clanParticipants__rowContainer");

                WarStatsInputDto warStatsInputDto = WarStatsInputDto.builder()
                        .leagueName(leagueFakeName)
                        .startDate(LocalDateTime.now())
                        .rank(rank)
                        .trophies(delta)
                        .totalTrophies(totalTrophies)
                        .build();
                warStatsInputDtos.add(warStatsInputDto);

                for (Element playerStat : playerStats) {
                    String tag = Arrays.stream(playerStat.select(".clanParticipants__row").get(1).select("a").attr("href").split("/"))
                            .reduce((first, second) -> second).orElseThrow(() -> new ParseException("Could not parse player's tag"));
                    String name = playerStat.select(".clanParticipants__row").get(1).select("a").text();

                    String[] battlesAsString = playerStat.select(".clanParticipants__row").stream()
                            .filter(element -> element.select(".clanParticipants__battleIcon").size() == 1)
                            .findFirst()
                            .orElseThrow(() -> new ParseException("Could not find battles for player" + name)).text().split("/");

                    String winsAsString = playerStat.select(".clanParticipants__row").stream()
                            .filter(element -> element.select(".clanParticipants__winIcon").size() == 1)
                            .findFirst()
                            .orElseThrow(() -> new ParseException("Could not find wins for player" + name)).text();

                    String cardsAsString = playerStat.select(".clanParticipants__row").stream()
                            .filter(element -> element.select(".clanParticipants__cardsIcon").size() == 1)
                            .findFirst()
                            .orElseThrow(() -> new ParseException("Could not find totalCards for player" + name)).text();

                    int battlesPlayed = Integer.valueOf(battlesAsString[0]);
                    int battlesGranted = Integer.valueOf(battlesAsString[1]);
                    int wins = Integer.valueOf(winsAsString);
                    int cards = Integer.valueOf(cardsAsString);

                    warStatsInputDto.getPlayerWarStats().add(
                            WarStatsInputDto.PlayerWarStatInputDto.builder()
                                    .name(name)
                                    .tag(tag)
                                    .cards(cards)
                                    .gamesGranted(battlesGranted)
                                    .gamesWon(wins)
                                    .gamesLost(battlesPlayed - wins)
                                    .gamesNotPlayed(battlesGranted - battlesPlayed).build());
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
            String clanTagInDiv = Arrays.stream(clanDiv.select(".clanWarHistory__row")
                    .get(1).select("a").attr("href").split("/")).reduce((first, second) -> second).orElse("");
            if (clanTag.equals(clanTagInDiv)) {
                return clanDiv;
            }
        }
        throw new ParseException("Could not find the clan's div");
    }


    @Override
    public boolean refresh() {
        try {
            URL url = new URL(siteConfigurationService.getRefreshUrl());
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/61.0.3163.100 Safari/537.36");
            logger.info("Refreshed with response code : " + con.getResponseCode());
            Map<String, String> resultAsMap = objectMapper.readValue(con.getInputStream(), new TypeReference<Map<String, String>>() {
            });
            if (!"true".equals(resultAsMap.get("success"))) {
                logger.warn("Site was not refreshed, data may not be up to date");
                return false;
            }


        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        return true;
    }


    public String getClanTag() {
        return clanTag;
    }

    public void setClanTag(String clanTag) {
        this.clanTag = clanTag;
    }

}
