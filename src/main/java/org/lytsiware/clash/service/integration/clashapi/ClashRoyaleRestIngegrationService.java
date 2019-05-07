package org.lytsiware.clash.service.integration.clashapi;

import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.service.integration.SiteConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.core.io.UrlResource;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;

@Service
public class ClashRoyaleRestIngegrationService {

    @Autowired
    ProxyService proxyService;

    private SiteConfigurationService siteConfigurationService;


    ClashRoyaleRestIngegrationService(SiteConfigurationService siteConfigurationService, ProxyService proxyService) {
        this.siteConfigurationService = siteConfigurationService;
        this.proxyService = proxyService;
    }

    public static void main(String[] args) throws MalformedURLException {
        SiteConfigurationService siteConfigurationService = new SiteConfigurationService(null, null, null, null, new UrlResource("http://welcome.usefixie.com"));
        ProxyService proxyService = new ProxyService("http://test:test@olympic.usefixie.com:80");
        proxyService.initProxy();
//        String connection = new ClashRoyaleRestIngegrationService(siteConfigurationService, proxyService).createConnection();
//        System.out.println(connection);
        new ClashRoyaleRestIngegrationService(siteConfigurationService, proxyService).test();

    }

    public WarLeague getWarLeagueStatsForCurrentWar() {
        return null;
    }

    private String createConnection() {
        URL url;
        try {
            url = siteConfigurationService.getClashRestUrl().getURL();
            URLConnection connection = url.openConnection(proxyService.getProxy());
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

    public void test() {
        RestTemplateCustomizer proxyCustomizer = restTemplate -> {
            SimpleClientHttpRequestFactory clientRequestFactory = new SimpleClientHttpRequestFactory();
            clientRequestFactory.setProxy(proxyService.getProxy());
            restTemplate.setRequestFactory(clientRequestFactory);
        };
        RestTemplate restTemplate = new RestTemplateBuilder(proxyCustomizer).build();
        RequestEntity<Void> requestEntity = RequestEntity.get(URI.create("http://welcome.usefixie.com")).build();
        ResponseEntity<String> response = restTemplate.exchange(requestEntity, String.class);
        System.out.println(response.getBody());
    }


}
