package org.lytsiware.clash.service.integration.clashapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.service.integration.SiteConfigurationService;
import org.lytsiware.clash.utils.ContentLengthHttpInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ClashRoyaleRestIntegrationService {

    @Value("${BEARER}")
    String bearer;

    private SiteConfigurationService siteConfigurationService;

    @Autowired
    ClashRoyaleRestIntegrationService(SiteConfigurationService siteConfigurationService) {
        this.siteConfigurationService = siteConfigurationService;
    }

//    private String createConnection() {
//        URL url;
//        try {
//            url = siteConfigurationService.getClashRestUrl().getURL();
//            URLConnection connection = url.openConnection(proxy);
//            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
//                StringBuilder sb = new StringBuilder();
//                String inputLine;
//                while ((inputLine = br.readLine()) != null) {
//                    sb.append(inputLine).append("\n");
//                }
//                return sb.toString();
//            }
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }

    public RestTemplate createRestTemplate() {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .interceptors(new ContentLengthHttpInterceptor())
                .build();
        return restTemplate;
    }

    public CurrentWarDto getDataFromSite() {
        try {
            URL url = siteConfigurationService.getClashRestUrl().getURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Authorization", bearer);
            conn.setRequestProperty("accept", "application/json");
            ObjectMapper objectMapper = new ObjectMapper();
            CurrentWarDto currentWarDto = objectMapper.readValue(conn.getInputStream(), CurrentWarDto.class);
            log.info("{}", currentWarDto);
            return currentWarDto;

//            RestTemplate restTemplate = createRestTemplate();
//            RequestEntity<Void> requestEntity = RequestEntity.get(siteConfigurationService.getClashRestUrl().getURI())
//                    .header("Authorization", bearer)
//                    .header("accept", "application/json")
//                    .build();
//            ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<Map<String, Object>>() {
//            };
////            preRequest(restTemplate);
//            CurrentWarDto result = restTemplate.exchange(requestEntity, CurrentWarDto.class).getBody();
//            log.info("{}", result);
//            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //Does an http request before the https because basic proxy authentication is disabled in jvm by default causing a 407 error.
    //Bt doing an http request before the issue is resolved!!
    //More over the workaround to enable in during initialization does not work 100% because when heroku restarts the dyno this param
    //is not passed
//    private void preRequest(RestTemplate restTemplate) {
//        try {
//            RequestEntity<Void> requestEntity = RequestEntity.get(new URI("http://godwokens.herokuapp.com/views/clanRules.htm"))
//                    .build();
//            restTemplate.exchange(requestEntity, String.class).getBody();
//        } catch (Exception e) {
//            log.error("PRE REQUEST FAILED DUE TO  {}", e);
//        }
//    }

    public WarLeague createWarLeagueFromData(@Valid CurrentWarDto warDto) {
        LocalDateTime warStartDate = calculateStartDate(warDto);
        WarLeague warLeague = new WarLeague(warStartDate);
        warLeague.setName(warStartDate.toString());
        warDto.getParticipants().stream().map(this::createWarStatsDto).forEach(playerWarStat -> playerWarStat.setWarLeague(warLeague));
        return warLeague;
    }

    private PlayerWarStat createWarStatsDto(CurrentWarDto.Participant participant) {
        return PlayerWarStat.builder()
                .player(new Player(participant.getTag(), participant.getName(), null))
                .collectionPhaseStats(CollectionPhaseStats.builder()
                        .gamesPlayed(participant.getCollectionDayBattlesPlayed())
                        .cardsWon(participant.getCardsEarned())
                        .build())
                .build();
    }

    private LocalDateTime calculateStartDate(CurrentWarDto warDto) {
        if (warDto.getState() == CurrentWarDto.State.COLLECTION_DAY) {
            return warDto.getCollectionEndTime().minusDays(1);
        } else {
            return warDto.getWarEndTime().minusDays(2);
        }
    }
}
