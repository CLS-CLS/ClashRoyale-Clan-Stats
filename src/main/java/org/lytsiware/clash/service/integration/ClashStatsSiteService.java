package org.lytsiware.clash.service.integration;

import org.lytsiware.clash.domain.player.PlayerWeeklyStats;

import java.util.List;

public interface ClashStatsSiteService {

	List<PlayerWeeklyStats> retrieveData();

}
