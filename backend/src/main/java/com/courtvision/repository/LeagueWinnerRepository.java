package com.courtvision.repository;

import com.courtvision.entity.League;
import com.courtvision.entity.LeagueWinner;
import com.courtvision.entity.TransactionStatus;
import com.courtvision.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for LeagueWinner entity
 * Provides database operations for league winners and oracle submissions
 */
@Repository
public interface LeagueWinnerRepository extends JpaRepository<LeagueWinner, Long> {

    /**
     * Find the winner for a specific league
     */
    Optional<LeagueWinner> findByLeagueId(Long leagueId);

    /**
     * Find winner for a specific league and user
     */
    Optional<LeagueWinner> findByLeagueIdAndWinnerId(Long leagueId, Long userId);

    /**
     * Find all winners for a specific user (user's league wins)
     */
    List<LeagueWinner> findByWinnerId(Long userId);

    /**
     * Find all winners announced within a date range
     */
    @Query("SELECT w FROM LeagueWinner w WHERE w.announcedAt BETWEEN :startDate AND :endDate ORDER BY w.announcedAt DESC")
    List<LeagueWinner> findByAnnouncedAtBetween(
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find all pending transactions waiting for Solana confirmation
     */
    @Query("SELECT w FROM LeagueWinner w WHERE w.transactionStatus IN ('PENDING', 'SUBMITTED') ORDER BY w.createdAt ASC")
    List<LeagueWinner> findPendingTransactions();

    /**
     * Find all failed transactions
     */
    @Query("SELECT w FROM LeagueWinner w WHERE w.transactionStatus = 'FAILED' ORDER BY w.updatedAt DESC")
    List<LeagueWinner> findFailedTransactions();

    /**
     * Check if a winner was already announced for a league
     */
    boolean existsByLeagueId(Long leagueId);

    /**
     * Find all winners with a specific transaction status
     */
    List<LeagueWinner> findByTransactionStatus(TransactionStatus status);

    /**
     * Find winner by Solana transaction hash
     */
    Optional<LeagueWinner> findBySolanaTransactionHash(String txHash);

    /**
     * Count winners for a user
     */
    long countByWinnerId(Long userId);
}
