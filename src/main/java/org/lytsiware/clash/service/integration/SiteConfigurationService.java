package org.lytsiware.clash.service.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class SiteConfigurationService {

    @Value("${clientDataUrl}")
    private Resource dataResource;

    @Value("${clientDataRefreshUrl}")
    private String refreshUrl;

    @Value("${deckshopClanUrl}")
    private Resource deckshopClanUrlResource;

    @Value("${statsRoyaleWarUrl}")
    private String statsRoyeleWarUrl;

    public SiteConfigurationService(){

    }

    public SiteConfigurationService(Resource dataResource, String refreshUrl, Resource deckshopClanUrlResource, String statsRoyeleWarUrl) {
        this.dataResource = dataResource;
        this.refreshUrl = refreshUrl;
        this.deckshopClanUrlResource = deckshopClanUrlResource;
        this.statsRoyeleWarUrl = statsRoyeleWarUrl;
    }

    public Resource getDataResource() {
        return dataResource;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }

    public Resource getDeckshopClanUrlResource() {
        return deckshopClanUrlResource;
    }

    public String getStatsRoyeleWarUrl() {
        return statsRoyeleWarUrl;
    }
}
