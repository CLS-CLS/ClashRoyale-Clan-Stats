package org.lytsiware.clash.service;

import java.util.List;

import org.lytsiware.clash.domain.player.PlayerWeeklyStats;

public interface IClashSiteService {

	List<PlayerWeeklyStats> retrieveData();

}
