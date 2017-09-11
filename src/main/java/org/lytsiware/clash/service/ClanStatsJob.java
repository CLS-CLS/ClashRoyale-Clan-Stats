package org.lytsiware.clash.service;

public class ClanStatsJob {

    IClanStatsService clanStatsService;


    public void run() {
       clanStatsService.updateDatabaseWithLatest();
    }

}
