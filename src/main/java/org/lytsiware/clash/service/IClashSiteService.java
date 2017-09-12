package org.lytsiware.clash.service;

import org.lytsiware.clash.domain.player.PlayerWeeklyStats;

import java.util.List;

public interface IClashSiteService {

	List<PlayerWeeklyStats> retrieveData();

}
