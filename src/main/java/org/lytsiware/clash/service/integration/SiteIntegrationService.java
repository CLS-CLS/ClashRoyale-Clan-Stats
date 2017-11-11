package org.lytsiware.clash.service.integration;

import java.util.List;

import org.lytsiware.clash.domain.playerweeklystats.PlayerWeeklyStats;

public interface SiteIntegrationService {

	List<PlayerWeeklyStats> retrieveData();

}
