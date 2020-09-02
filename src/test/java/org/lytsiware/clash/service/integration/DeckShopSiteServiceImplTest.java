package org.lytsiware.clash.service.integration;

import org.junit.Test;
import org.lytsiware.clash.service.donations.integration.DeckShopSiteServiceImpl;
import org.springframework.core.io.FileSystemResource;

public class DeckShopSiteServiceImplTest {

    DeckShopSiteServiceImpl deckShopSiteService = new DeckShopSiteServiceImpl(new SiteConfigurationService(null, null, new FileSystemResource("src/test/resources/deckshop.html"), null, null));

    @Test
    public void retrieveData() throws Exception {

        deckShopSiteService.retrieveData();

    }

}