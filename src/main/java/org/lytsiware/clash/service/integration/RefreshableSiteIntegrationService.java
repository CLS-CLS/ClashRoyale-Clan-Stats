package org.lytsiware.clash.service.integration;

public interface RefreshableSiteIntegrationService<R> extends SiteIntegrationService<R> {

    R retrieveData(boolean requestRefresh);

    boolean refresh();
}
