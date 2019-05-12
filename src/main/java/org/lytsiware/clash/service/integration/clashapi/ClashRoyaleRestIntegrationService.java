package org.lytsiware.clash.service.integration.clashapi;

import org.lytsiware.clash.Application;
import org.lytsiware.clash.domain.player.Player;
import org.lytsiware.clash.domain.war.league.WarLeague;
import org.lytsiware.clash.domain.war.playerwarstat.CollectionPhaseStats;
import org.lytsiware.clash.domain.war.playerwarstat.PlayerWarStat;
import org.lytsiware.clash.service.integration.SiteConfigurationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.UrlResource;
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
public class ClashRoyaleRestIntegrationService {

    Proxy proxy;
    String bearer =  //"Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6IjA4ZjUzYzRlLWVmYjEtNDQxYy1iYzYyLTQ0Njc4YjQ1Mjg4ZSIsImlhdCI6MTU1NzI1NDY4NSwic3ViIjoiZGV2ZWxvcGVyLzlmYjlkNTExLTI5ZjgtODcwNC02YmM2LWMwZDZmODM5YWE5MCIsInNjb3BlcyI6WyJyb3lhbGUiXSwibGltaXRzIjpbeyJ0aWVyIjoiZGV2ZWxvcGVyL3NpbHZlciIsInR5cGUiOiJ0aHJvdHRsaW5nIn0seyJjaWRycyI6WyI5OS44MC4xODMuMTE3IiwiOTkuODEuMTM1LjMyIl0sInR5cGUiOiJjbGllbnQifV19.8CWIVewGPTXRYppyI6M21-NzSICGNmdRooPpGeHm-YRb3S4UAOtR0PYeTh5Gxhl1oThLUrhUgRMnelDuVQudcw";
            "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6ImJhZDQ4MWIwLTJkYmUtNDZkNC1iMDA2LWEwYWI0MTZkODQ4MiIsImlhdCI6MTU1NzU3MzU1MSwic3ViIjoiZGV2ZWxvcGVyLzlmYjlkNTExLTI5ZjgtODcwNC02YmM2LWMwZDZmODM5YWE5MCIsInNjb3BlcyI6WyJyb3lhbGUiXSwibGltaXRzIjpbeyJ0aWVyIjoiZGV2ZWxvcGVyL3NpbHZlciIsInR5cGUiOiJ0aHJvdHRsaW5nIn0seyJjaWRycyI6WyIzNy42LjExNC4zNiJdLCJ0eXBlIjoiY2xpZW50In1dfQ.XGzDji74vSwPit7Z56agB5Jd1kabkimPO7uDB3G_ug3rpiw6Y7WYFZKWpLfA4js9Y2FeXHtQ5UN_Z4q-Czm8yQ";
    private SiteConfigurationService siteConfigurationService;


    ClashRoyaleRestIntegrationService(SiteConfigurationService siteConfigurationService, @Autowired @Qualifier("fixie") Proxy proxy) {
        this.siteConfigurationService = siteConfigurationService;
        this.proxy = proxy;
    }

    public static void main2(String[] args) throws Exception {
        SiteConfigurationService siteConfigurationService = new SiteConfigurationService(null, null, null, null,
                new UrlResource("https://api.clashroyale.com/v1/clans/%23PLVV88G2/currentwar"));

        Application.Config config = new Application.Config() {
            @Override
            public String getFixieUrl() {
                return "http://fixie:tzdVUBEc2WIdrqC@olympic.usefixie.com:80";
            }
        };

        ClashRoyaleRestIntegrationService service = new ClashRoyaleRestIntegrationService(siteConfigurationService, config.fixieProxy());
        service.getDataFromSite();
    }

    public static void main(String[] args) throws Exception {
        SiteConfigurationService siteConfigurationService = new SiteConfigurationService(null, null, null, null,
                new UrlResource("https://api.clashroyale.com/v1/clans/%23PLVV88G2/currentwar"));


        ClashRoyaleRestIntegrationService service = new ClashRoyaleRestIntegrationService(siteConfigurationService, Proxy.NO_PROXY);

        System.out.println(service.getDataFromSite());
    }

    public WarLeague getWarLeagueStatsForCurrentWar() {
        return createWarLeagueFromData(getDataFromSite());
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
            return restTemplate.exchange(requestEntity, CurrentWarDto.class).getBody();
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
                .player(new Player(participant.getTag(), null, null))
                .collectionPhaseStats(CollectionPhaseStats.builder()
                        .gamesPlayed(participant.getCollectionDayBattlesPlayed())
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
