package org.lytsiware.clash.service.war;

import org.lytsiware.clash.dto.war.input.WarStatsInputDto;

import java.util.List;

public interface WarInputService {

    List<WarStatsInputDto> getWarStatsFromSite();

    void updateOrSaveWarStats();

    List<WarStatsInputDto> getPlayerWarStatsForInput(boolean includeNotParticipating);


}
