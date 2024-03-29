package org.lytsiware.clash.war2.repository;

import org.lytsiware.clash.war2.repository.dto.RiverRaceAggregateDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AggregationRepository {

    @Autowired
    EntityManager em;

    public List<RiverRaceAggregateDto> getAggregatedStats(int delta, int limit, String clanTag) {
        String queryString = "select tag, " +
                " round(avg(fame)) as fame, " +
                " round(avg(active_fame)) as active_fame, " +
                " round(avg(repair_points)) as repair_points," +
                " round(avg(active_fame + repair_points)) as score, " +
                " sum(war_decks) as total_war_decks, " +
                " sum(required_decks) as total_required_decks " +
                " from river_race_participant where river_race_clan_fk in " +
                " (select id from river_race_clan " +
                " where tag= :clanTag and id in " +
                " (select clan_id from river_race  " +
                " where super_cell_created_date is not null " +
                " order by super_cell_created_date desc limit :limit offset :delta ))" +
                " group by tag";
        List<Object[]> result = em.createNativeQuery(queryString).setParameter("delta", delta)
                .setParameter("limit", limit)
                .setParameter("clanTag", clanTag).getResultList();
        return result.stream().map(RiverRaceAggregateDto::of).collect(Collectors.toList());

    }

}
