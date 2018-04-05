package org.lytsiware.clash.service.integration;

import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;

import java.util.List;

public interface RefreshableSiteIntegrationService extends SiteIntegrationService {

    List<PlayerWeeklyStats> retrieveData(boolean requestRefresh);

}
