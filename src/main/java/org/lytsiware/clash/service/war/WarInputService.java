package org.lytsiware.clash.service.war;


import org.lytsiware.clash.dto.war.input.WarStatsInputDto;
import org.lytsiware.clash.service.integration.SiteIntegrationService;
import org.lytsiware.clash.service.integration.SiteQualifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WarInputService {


    @Autowired
    @SiteQualifier(SiteQualifier.Name.STATS_ROYALE_WAR)
    SiteIntegrationService<List<WarStatsInputDto>> siteIntegrationService;


    public List<WarStatsInputDto> getWarStatsFromSite() {
        return siteIntegrationService.retrieveData();
    }


}
