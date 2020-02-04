CREATE OR REPLACE VIEW public.global_player_stats AS
 SELECT wow.tag,
 	wow.name,
	wow.role,
	wow.in_clan,
	wow.eligible as eligible_games,
	wow.played as played_war_games,
	round(wow.played::numeric/wow.eligible, 3) as participation_ratio,
    wow.abandoned as abandoned_war_games, wow.abandoned_collection_games,
        CASE
            WHEN wow.played > 0 THEN round(wow.abandoned::numeric / wow.played::numeric, 3)
            ELSE 0::numeric
        END AS abandoned_war_games_ratio,
        CASE
            WHEN wow.played > 0 THEN round(wow.abandoned_collection_games::numeric / (wow.played::numeric * 3)::numeric, 3)
            ELSE 0::numeric
        END AS abandoned_collection_games_ratio
   FROM ( SELECT p.tag, p.name, p.role, p.in_clan, played.player_tag,
            played.eligible, played.played, played.abandoned,
            played.abandoned_collection_games
           FROM ( SELECT participation.player_tag,
                    count(participation.player_tag) AS eligible,
                    count(participation.participated) AS played,
                    sum(participation.abandoned) AS abandoned,
                    sum(participation.abandoned_collection_games) AS abandoned_collection_games
                   FROM ( SELECT player_war_stat.player_tag,
                                CASE
                                    WHEN player_war_stat.games_granted > 0 THEN 1
                                    ELSE NULL::integer
                                END AS participated,
                                CASE
                                    WHEN player_war_stat.games_granted >= 0 THEN (player_war_stat.games_granted - player_war_stat.war_games_lost - player_war_stat.war_games_won)::integer
                                    ELSE 0
                                END AS abandoned,
                                CASE
                                    WHEN player_war_stat.collection_games_played > 0 THEN 3 - player_war_stat.collection_games_played
                                    ELSE 0
                                END AS abandoned_collection_games
                           FROM player_war_stat) participation
                  GROUP BY participation.player_tag) played
      JOIN player p ON p.tag::text = played.player_tag::text
     ORDER BY played.played DESC) wow;

