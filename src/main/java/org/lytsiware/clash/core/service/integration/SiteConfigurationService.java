package org.lytsiware.clash.core.service.integration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Getter
public class SiteConfigurationService {

    @Value("${clientDataUrl}")
    private Resource dataResource;

    @Value("${clientDataRefreshUrl}")
    private String refreshUrl;

    @Value("${deckshopClanUrl}")
    private Resource deckshopClanUrlResource;

    @Value("${statsRoyaleWarUrl}")
    private String statsRoyeleWarUrl;

    @Value("${clashRestUrl}")
    private Resource clashRestUrl;

    public SiteConfigurationService() {

    }

}
