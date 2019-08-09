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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.IOException;
import java.net.Proxy;
import java.net.URI;
import java.time.LocalDateTime;

@Service
@Slf4j
public class ClashRoyaleRestIntegrationService {

    @Value("${BEARER}")
    String bearer;

    @Autowired
    @Qualifier("fixie")
    Proxy proxy;

    @Autowired
    private ObjectMapper objectMapper;

    private SiteConfigurationService siteConfigurationService;

    @Autowired
    ClashRoyaleRestIntegrationService(SiteConfigurationService siteConfigurationService) {
        this.siteConfigurationService = siteConfigurationService;
    }


    public RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);
        RestTemplate restTemplate = new RestTemplateBuilder()
                .requestFactory(() -> requestFactory)
                .interceptors(new ContentLengthHttpInterceptor())
                .build();
        return restTemplate;
    }

    public CurrentWarDto getDataFromSite() {
        try {
            RestTemplate restTemplate = createRestTemplate();
            RequestEntity<Void> requestEntity = RequestEntity.get(siteConfigurationService.getClashRestUrl().getURI())
                    .header("Authorization", bearer)
                    .header("accept", "application/json")
                    .build();
            preRequest(restTemplate);
            CurrentWarDto result = restTemplate.exchange(requestEntity, CurrentWarDto.class).getBody();
            log.info("{}", result);
            return result;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Does an http request before the https because basic proxy authentication is disabled in jvm by default causing a 407 error.
     * Bt doing an http request before the issue is resolved!!
     * More over the workaround to enable in during initialization does not work 100% because when heroku restarts the dyno this param
     * is not passed
     */
    private void preRequest(RestTemplate restTemplate) {
        try {
            RequestEntity<Void> requestEntity = RequestEntity.get(new URI("http://godwokens.herokuapp.com/views/clanRules.htm"))
                    .build();
            restTemplate.exchange(requestEntity, String.class).getBody();
        } catch (Exception e) {
            log.error("PRE REQUEST FAILED DUE TO  {}", e);
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
