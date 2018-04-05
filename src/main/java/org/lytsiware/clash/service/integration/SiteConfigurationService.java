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

    public SiteConfigurationService(){

    }

    public SiteConfigurationService(Resource dataResource, String refreshUrl, Resource deckshopClanUrlResource) {
        this.dataResource = dataResource;
        this.refreshUrl = refreshUrl;
        this.deckshopClanUrlResource = deckshopClanUrlResource;
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
}
