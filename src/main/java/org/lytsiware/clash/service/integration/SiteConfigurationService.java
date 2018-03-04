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

    public Resource getDataResource() {
        return dataResource;
    }

    public String getRefreshUrl() {
        return refreshUrl;
    }
}