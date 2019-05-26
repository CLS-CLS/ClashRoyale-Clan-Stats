package org.lytsiware.clash.service.integration.clashapi;

import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.service.integration.SiteConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class ClashRoyaleRestIntegrationService {

    @Value("${BEARER}")
    String bearer;

    Proxy proxy;

    private SiteConfigurationService siteConfigurationService;


    ClashRoyaleRestIntegrationService(SiteConfigurationService siteConfigurationService, @Autowired @Qualifier("fixie") Proxy proxy) {
        this.siteConfigurationService = siteConfigurationService;
        this.proxy = proxy;
    }

    private String createConnection() {
        URL url;
        try {
            url = siteConfigurationService.getClashRestUrl().getURL();
            URLConnection connection = url.openConnection(proxy);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder sb = new StringBuilder();
                String inputLine;
                while ((inputLine = br.readLine()) != null) {
                    sb.append(inputLine).append("\n");
                }
                return sb.toString();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public RestTemplate createRestTemplate() {
        RestTemplateCustomizer proxyCustomizer = restTemplate -> {
            SimpleClientHttpRequestFactory clientRequestFactory = new SimpleClientHttpRequestFactory();
            clientRequestFactory.setProxy(proxy);
            restTemplate.setRequestFactory(clientRequestFactory);
        };
        RestTemplate restTemplate = new RestTemplateBuilder(proxyCustomizer).build();
        return restTemplate;
    }

    public CurrentWarDto getDataFromSite() {
        try {
            RestTemplate restTemplate = createRestTemplate();
            RequestEntity<Void> requestEntity = RequestEntity.get(siteConfigurationService.getClashRestUrl().getURI())
                    .header("authorization", bearer)
                    .header("accept", "application/json")
                    .build();
            ParameterizedTypeReference<Map<String, Object>> type = new ParameterizedTypeReference<Map<String, Object>>() {
            };
            CurrentWarDto result = restTemplate.exchange(requestEntity, CurrentWarDto.class).getBody();
            log.info("{}", result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
