package org.lytsiware.clash.donation.service;

import org.lytsiware.clash.Constants;
import org.lytsiware.clash.Week;
import org.lytsiware.clash.donation.domain.PlayerWeeklyStats;

import java.util.List;

public interface DonationAggregationService {

    /**
     * Calculates the averages of the past {@link Constants#AVG_WEEKS weeks (the provided week included
     * as one of these weeks)
     *
     * @param week
     * @return
     */
    List<PlayerWeeklyStats> calculateAvgs(Week week);

    /**
     * Recaclulates the averages for the provided week and updates the db
     * @param week
     */
    void calculateAndSaveAvgs(Week week);


}
