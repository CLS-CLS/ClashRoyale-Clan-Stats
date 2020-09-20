package org.lytsiware.clash;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.lytsiware.clash.core.service.integration.proxy.ProxyAndBearerHolder;
import org.lytsiware.clash.war2.service.integration.ExchangeHelperService;
import org.lytsiware.clash.war2.service.integration.War2CRLIntegrationService;
import org.lytsiware.clash.war2.service.integration.dto.RiverRaceCurrentDto;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.Proxy;
import java.util.Properties;

public class War2CRLIntegrationServiceTest {

    private War2CRLIntegrationService war2CRLIntegrationService;

    ProxyAndBearerHolder proxyAndBearerHolder() {
        return new ProxyAndBearerHolder() {
            @Override
            public String getBearer() {
                return "Bearer xxx";
            }

            @Override
            public Proxy getProxy() {
                return Proxy.NO_PROXY;
            }
        };
    }

    @Before
    public void setUp() throws IOException {
        Properties p = new Properties();
        p.load(this.getClass().getResourceAsStream("/application-test.properties"));
        war2CRLIntegrationService = new War2CRLIntegrationService(
                new ExchangeHelperService(proxyAndBearerHolder(), new ObjectMapper()));
        ReflectionTestUtils.setField(war2CRLIntegrationService, "riverRaceCurrentUrl", p.getProperty("riverRaceCurrentUrl"));
        ReflectionTestUtils.setField(war2CRLIntegrationService, "riverRaceLogUrl", p.getProperty("riverRaceLogUrl"));
    }

    @Test
    public void testCurrentRiverRaceConnection() {
        RiverRaceCurrentDto riverRaceCurrentDto = war2CRLIntegrationService.getCurrentRiverRace();
        System.out.println(riverRaceCurrentDto);
    }

    @Test
    public void testRiverRaceLogConnection() {
        System.out.println(war2CRLIntegrationService.getRiverRaceLog(1));
    }


}