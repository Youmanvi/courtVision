package com.courtvision.repository;

import com.courtvision.entity.ScoreCalculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ScoreCalculation entity
 */
@Repository
public interface ScoreCalculationRepository extends JpaRepository<ScoreCalculation, Long> {

    /**
     * Find latest score for a user in a league
     */
    Optional<ScoreCalculation> findFirstByLeagueIdAndUserIdOrderByCalculatedAtDesc(Long leagueId, Long userId);

    /**
     * Find all scores for a league ordered by score descending
     */
    @Query("SELECT s FROM ScoreCalculation s WHERE s.league.id = :leagueId " +
           "AND s.calculatedAt = (SELECT MAX(sc.calculatedAt) FROM ScoreCalculation sc WHERE sc.league.id = s.league.id AND sc.user.id = s.user.id) " +
           "ORDER BY s.totalScore DESC")
    List<ScoreCalculation> findLatestLeagueScoresOrdered(@Param("leagueId") Long leagueId);

    /**
     * Find all scores for a user across leagues
     */
    List<ScoreCalculation> findByUserIdOrderByCalculatedAtDesc(Long userId);

    /**
     * Find scores calculated within a time range
     */
    List<ScoreCalculation> findByCalculatedAtBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Find scores for a specific league and date
     */
    @Query("SELECT s FROM ScoreCalculation s WHERE s.league.id = :leagueId " +
           "AND CAST(s.calculatedAt AS java.time.LocalDate) = CAST(:date AS java.time.LocalDate)")
    List<ScoreCalculation> findScoresForLeagueOnDate(@Param("leagueId") Long leagueId, @Param("date") LocalDateTime date);

    /**
     * Check if scores have been calculated for a league today
     */
    @Query("SELECT COUNT(s) FROM ScoreCalculation s WHERE s.league.id = :leagueId " +
           "AND CAST(s.calculatedAt AS java.time.LocalDate) = CURRENT_DATE")
    long countScoresCalculatedToday(@Param("leagueId") Long leagueId);
}
