package org.lytsiware.clash.war.service.integration.clashapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.core.domain.player.Player;
import org.lytsiware.clash.core.service.integration.SiteConfigurationService;
import org.lytsiware.clash.core.service.integration.proxy.ProxyAndBearerHolder;
import org.lytsiware.clash.utils.ContentLengthHttpInterceptor;
import org.lytsiware.clash.war.domain.league.WarLeague;
import org.lytsiware.clash.war.domain.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.war.domain.playerwarstat.PlayerWarStat;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClashRoyaleRestIntegrationService {

    private final ProxyAndBearerHolder proxyAndBearerHolder;

    private final SiteConfigurationService siteConfigurationService;



    public RestTemplate createRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        log.info("Using proxy with URL: {}", proxyAndBearerHolder.getProxy().address());
        requestFactory.setProxy(proxyAndBearerHolder.getProxy());
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
                    .header("Authorization", proxyAndBearerHolder.getBearer())
                    .header("accept", "application/json")
                    .build();
            preRequest(restTemplate);
            CurrentWarDto result = restTemplate.exchange(requestEntity, CurrentWarDto.class).getBody();
            log.debug("{}", result);
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
            RequestEntity<Void> requestEntity = RequestEntity.get(new URI("http://godwokens.herokuapp.com/view/clanRules.htm"))
                    .build();
            restTemplate.exchange(requestEntity, String.class).getBody();
        } catch (Exception e) {
            log.error("PRE REQUEST FAILED DUE TO  {}", e);
        }
    }

    public Optional<WarLeague> createWarLeagueFromData(@Valid CurrentWarDto warDto) {
        LocalDateTime warStartDate = calculateStartDate(warDto);
        if (warStartDate == null) {
            return Optional.empty();
        }
        WarLeague warLeague = new WarLeague(warStartDate);
        warLeague.setName(warStartDate.toString());
        warDto.getParticipants().stream().map(this::createWarStatsDto).forEach(playerWarStat -> playerWarStat.setWarLeague(warLeague));
        return Optional.of(warLeague);
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
        } else if (warDto.getState() == CurrentWarDto.State.WAR_DAY) {
            return warDto.getWarEndTime().minusDays(2);
        } else return null;
    }
}
