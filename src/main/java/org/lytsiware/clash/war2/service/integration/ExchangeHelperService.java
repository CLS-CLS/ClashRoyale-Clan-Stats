package org.lytsiware.clash.war2.service.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lytsiware.clash.core.service.integration.proxy.ProxyAndBearerHolder;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.RequestEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@AllArgsConstructor
@Slf4j
public class ExchangeHelperService {

    private final ProxyAndBearerHolder proxyAndBearerHolder;
    private final ObjectMapper objectMapper;

    public RiverRaceCurrentDto exchangeCurrentRiverRace(URI riverRaceCurrentUrl) {
        RestTemplate restTemplate = restTemplate();
        RequestEntity<Void> requestEntity = RequestEntity.get(riverRaceCurrentUrl)
                .header("Authorization", proxyAndBearerHolder.getBearer())
                .header("accept", "application/json")
                .build();
        preRequest(restTemplate);
        RiverRaceCurrentDto result = restTemplate.exchange(requestEntity, RiverRaceCurrentDto.class).getBody();
        return result;
    }

    public RiverRaceLogDto exchangeRiverRaceLog(URI riverRaceLogUrl) {
        RestTemplate restTemplate = restTemplate();
        RequestEntity<Void> requestEntity = RequestEntity.get(
                riverRaceLogUrl)
                .header("Authorization", proxyAndBearerHolder.getBearer())
                .header("accept", "application/json")
                .build();
        preRequest(restTemplate);
        RiverRaceLogDto result = restTemplate.exchange(requestEntity, RiverRaceLogDto.class).getBody();
        return result;
    }

    private RestTemplate restTemplate() {
        log.info("Using proxy with URL: {}", proxyAndBearerHolder.getProxy().address());
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxyAndBearerHolder.getProxy());
        return new RestTemplateBuilder()
                .requestFactory(() -> requestFactory)
                .build();
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
}
