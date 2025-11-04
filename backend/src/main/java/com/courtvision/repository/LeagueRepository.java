package com.courtvision.repository;

import com.courtvision.entity.League;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueRepository extends JpaRepository<League, Long> {

    /**
     * Find all leagues created by a specific user
     */
    List<League> findByCreatorId(Long creatorId);

    /**
     * Find all leagues where a user is a member (including as creator)
     */
    @Query("SELECT DISTINCT l FROM League l " +
           "LEFT JOIN l.creator u1 " +
           "LEFT JOIN LeagueMember lm ON lm.league = l " +
           "WHERE u1.id = :userId OR lm.user.id = :userId")
    List<League> findAllLeaguesForUser(@Param("userId") Long userId);

    /**
     * Find a league by ID with creator details
     */
    @Query("SELECT l FROM League l " +
           "LEFT JOIN FETCH l.creator " +
           "WHERE l.id = :leagueId")
    Optional<League> findByIdWithCreator(@Param("leagueId") Long leagueId);

    /**
     * Find active leagues only
     */
    List<League> findByStatusAndCreatorId(League.LeagueStatus status, Long creatorId);

    /**
     * Check if league name exists for a creator
     */
    boolean existsByNameAndCreatorId(String name, Long creatorId);
}
