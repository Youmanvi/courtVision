package com.courtvision.repository;

import com.courtvision.entity.LeagueMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LeagueMemberRepository extends JpaRepository<LeagueMember, Long> {

    /**
     * Find all members of a specific league
     */
    List<LeagueMember> findByLeagueId(Long leagueId);

    /**
     * Find a specific member in a league
     */
    Optional<LeagueMember> findByLeagueIdAndUserId(Long leagueId, Long userId);

    /**
     * Find all leagues a user is a member of
     */
    @Query("SELECT DISTINCT lm.league FROM LeagueMember lm WHERE lm.user.id = :userId")
    List<LeagueMember> findAllByUserId(@Param("userId") Long userId);

    /**
     * Count members in a league
     */
    long countByLeagueId(Long leagueId);

    /**
     * Check if user is already a member of the league
     */
    boolean existsByLeagueIdAndUserId(Long leagueId, Long userId);

    /**
     * Check if user is owner of the league (creator)
     */
    boolean existsByLeagueIdAndUserIdAndRole(Long leagueId, Long userId, LeagueMember.MemberRole role);
}
