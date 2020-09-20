package org.lytsiware.clash.war2.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lytsiware.clash.core.service.integration.proxy.ProxyAndBearerHolder;
import org.lytsiware.clash.war2.domain.RiverRace;
import org.lytsiware.clash.war2.repository.RiverRaceRepository;
import org.lytsiware.clash.war2.service.integration.ExchangeHelperService;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceLogDto;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.net.Proxy;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class RiverRaceServiceIT {

    @Autowired
    RiverRaceInternalService riverRaceService;


    @Autowired
    RiverRaceRepository repository;

    @Autowired
    EntityManager em;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    ExchangeHelperService exchangeHelperService;


    @Test
    @Transactional
    public void testRiverRaceFlow() throws IOException, URISyntaxException {

        riverRaceUpdateNotFinished();

        riverRaceUpdate2Finished();

        riverRaceUpdate3TestAfterFinish();

        riverRaceUpdate4Finalize();

    }

    @Test
    @Transactional
    public void testRiverRaceExcepionMissingSection() {

        //given
        em.persist(RiverRace.builder().sectionIndex(1).active(true).build());
        Mockito.when(exchangeHelperService.exchangeRiverRaceLog(any()))
                .thenReturn(RiverRaceLogDto.builder()
                        .items(Collections.singletonList(RiverRaceLogDto.RiverRaceWeekDto.builder()
                                .sectionIndex(89)
                                .build()))
                        .build()
                );

        //when
        Exception ex = null;
        try {
            riverRaceService.finalizeRace("whatever");
        } catch (SectionIndexMissmatchException e) {
            ex = e;
        }

        Assert.assertNotNull(ex);
    }


    private void riverRaceUpdate4Finalize() throws IOException, URISyntaxException {
        //given
        String jsonAnswer = readFile("war2/riverracelog.json");
        RiverRaceLogDto answer = objectMapper.readValue(jsonAnswer, RiverRaceLogDto.class);
        Mockito.when(exchangeHelperService.exchangeRiverRaceLog(any())).thenReturn(answer);

        //when
        riverRaceService.finalizeRace("PG2000PL");

        //then
        RiverRace previousRace = repository.logRace(PageRequest.of(0, 1)).get(0);

        Assert.assertFalse(repository.activeRace().isPresent());
        Assert.assertFalse(previousRace.isActive());
        Assert.assertTrue(previousRace.isFinished());
        Assert.assertEquals(720, previousRace.getClan().getParticipants().get(0).getActiveFame());
        Assert.assertEquals(1523, previousRace.getClan().getParticipants().get(1).getFame());
        Assert.assertEquals(64, previousRace.getSeasonId());
        Assert.assertEquals(2, previousRace.getSectionIndex());
        Assert.assertEquals(1, previousRace.getClan().getRank());
        Assert.assertEquals(10, previousRace.getClan().getTrophyChange());


    }

    private void riverRaceUpdate3TestAfterFinish() throws IOException, URISyntaxException {
        //given
        String jsonAnswer3 = readFile("war2/riverracecurrent2.json");
        RiverRaceCurrentDto answer3 = objectMapper.readValue(jsonAnswer3, RiverRaceCurrentDto.class);
        answer3.getClan().getParticipants().get(0).setFame(1000);
        Mockito.when(exchangeHelperService.exchangeCurrentRiverRace(any())).thenReturn(answer3);

        //when
        riverRaceService.updateActiveRace();

        //then
        RiverRace activeRace = repository.activeRace().orElse(null);
        Assert.assertNotNull(activeRace);
        Assert.assertEquals(2, activeRace.getClans().size());
        Assert.assertTrue(activeRace.isActive());
        Assert.assertTrue(activeRace.isFinished());
        Assert.assertEquals(720, activeRace.getClan().getParticipants().get(0).getActiveFame());
        Assert.assertEquals(1000, activeRace.getClan().getParticipants().get(0).getFame());
    }

    private void riverRaceUpdate2Finished() throws IOException, URISyntaxException {
        //given
        String jsonAnswer2 = readFile("war2/riverracecurrent2.json");
        RiverRaceCurrentDto answer2 = objectMapper.readValue(jsonAnswer2, RiverRaceCurrentDto.class);
        Mockito.when(exchangeHelperService.exchangeCurrentRiverRace(any())).thenReturn(answer2);

        //when
        riverRaceService.updateActiveRace();

        //then
        RiverRace activeRace;
        activeRace = repository.activeRace().orElse(null);
        Assert.assertNotNull(activeRace);
        Assert.assertEquals(2, activeRace.getClans().size());
        Assert.assertTrue(activeRace.isActive());
        Assert.assertTrue(activeRace.isFinished());
        Assert.assertEquals(720, activeRace.getClan().getParticipants().get(0).getActiveFame());
        Assert.assertEquals(720, activeRace.getClan().getParticipants().get(0).getFame());
    }

    private void riverRaceUpdateNotFinished() throws IOException, URISyntaxException {
        String jsonAnswer = readFile("war2/riverracecurrent1.json");
        RiverRaceCurrentDto answer = objectMapper.readValue(jsonAnswer, RiverRaceCurrentDto.class);
        Mockito.when(exchangeHelperService.exchangeCurrentRiverRace(any())).thenReturn(answer);
        //when
        riverRaceService.updateActiveRace();

        //then
        RiverRace activeRace = repository.activeRace().orElse(null);
        Assert.assertNotNull(activeRace);
        Assert.assertEquals(2, activeRace.getClans().size());
        Assert.assertTrue(activeRace.isActive());
        Assert.assertFalse(activeRace.isFinished());
        Assert.assertEquals(705, activeRace.getClan().getParticipants().get(0).getActiveFame());
        Assert.assertEquals(705, activeRace.getClan().getParticipants().get(0).getFame());
    }

    private String readFile(String fileName) throws IOException, URISyntaxException {
        return Files.readAllLines(Paths.get(getClass().getClassLoader()
                .getResource(fileName)
                .toURI()))
                .stream()
                .collect(Collectors.joining("\r\n"));
    }


    @TestConfiguration
    public static class DataJpaTestConfiguration {

        @Bean
        ObjectMapper objectMapper() {
            ObjectMapper om = new ObjectMapper();
            om.findAndRegisterModules();
            om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return om;
        }

        @Bean
        ProxyAndBearerHolder proxyAndBearerHolder() {
            return new ProxyAndBearerHolder() {
                @Override
                public String getBearer() {
                    return "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiIsImtpZCI6IjI4YTMxOGY3LTAwMDAtYTFlYi03ZmExLTJjNzQzM2M2Y2NhNSJ9.eyJpc3MiOiJzdXBlcmNlbGwiLCJhdWQiOiJzdXBlcmNlbGw6Z2FtZWFwaSIsImp0aSI6IjNlMWU4OWM0LWU3ZmMtNDkxMC05NjQxLWY4NjIzODFhM2Q3NyIsImlhdCI6MTYwMDM3MTM2Nywic3ViIjoiZGV2ZWxvcGVyLzlmYjlkNTExLTI5ZjgtODcwNC02YmM2LWMwZDZmODM5YWE5MCIsInNjb3BlcyI6WyJyb3lhbGUiXSwibGltaXRzIjpbeyJ0aWVyIjoiZGV2ZWxvcGVyL3NpbHZlciIsInR5cGUiOiJ0aHJvdHRsaW5nIn0seyJjaWRycyI6WyI0Ni4xOTAuNTEuNDAiXSwidHlwZSI6ImNsaWVudCJ9XX0.uJ-IPpkkGVQPNDHeqYse-NZ3awjQSHCJn174Gri74pl30duT70G8LLPATYSm2Lt41YeS_xNcl_VTgFEY1R_EYg";
                }

                @Override
                public Proxy getProxy() {
                    return Proxy.NO_PROXY;
                }
            };
        }


    }

}