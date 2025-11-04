package com.courtvision.repository;

import com.courtvision.entity.Draft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DraftRepository extends JpaRepository<Draft, Long> {

    /**
     * Find draft by league ID
     */
    Optional<Draft> findByLeagueId(Long leagueId);

    /**
     * Find draft by league ID with eager loaded league
     */
    @Query("SELECT d FROM Draft d JOIN FETCH d.league WHERE d.league.id = :leagueId")
    Optional<Draft> findByLeagueIdWithLeague(@Param("leagueId") Long leagueId);

    /**
     * Find all drafts for a user's leagues
     */
    @Query("SELECT d FROM Draft d WHERE d.league.id IN " +
           "(SELECT lm.league.id FROM LeagueMember lm WHERE lm.user.id = :userId)")
    List<Draft> findAllDraftsForUser(@Param("userId") Long userId);

    /**
     * Check if a draft exists for a league
     */
    boolean existsByLeagueId(Long leagueId);
}
