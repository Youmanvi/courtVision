package com.courtvision.controller;

import com.courtvision.entity.LeagueWinner;
import com.courtvision.entity.TransactionStatus;
import com.courtvision.repository.LeagueWinnerRepository;
import com.courtvision.scheduler.WinnerAnnouncementScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Winner Controller
 * REST endpoints for managing league winners and Solana oracle submissions
 */
@Slf4j
@RestController
@RequestMapping("/api/winners")
@RequiredArgsConstructor
public class WinnerController {

    private final LeagueWinnerRepository leagueWinnerRepository;
    private final WinnerAnnouncementScheduler winnerAnnouncementScheduler;

    /**
     * Get league winner
     * GET /api/winners/leagues/{leagueId}
     */
    @GetMapping("/leagues/{leagueId}")
    public ResponseEntity<?> getLeagueWinner(@PathVariable Long leagueId) {
        try {
            LeagueWinner winner = leagueWinnerRepository.findByLeagueId(leagueId)
                .orElse(null);

            if (winner == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No winner announced for this league yet"
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "League winner retrieved successfully",
                "data", winner
            ));

        } catch (Exception e) {
            log.error("Error retrieving league winner", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error retrieving league winner"
            ));
        }
    }

    /**
     * Get all league winners for a user
     * GET /api/winners/users/{userId}
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserWins(@PathVariable Long userId) {
        try {
            List<LeagueWinner> wins = leagueWinnerRepository.findByWinnerId(userId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "User wins retrieved successfully",
                "data", wins,
                "count", wins.size()
            ));

        } catch (Exception e) {
            log.error("Error retrieving user wins", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error retrieving user wins"
            ));
        }
    }

    /**
     * Get all winners with pending transactions
     * GET /api/winners/pending
     * Requires ADMIN role
     */
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getPendingTransactions() {
        try {
            List<LeagueWinner> pending = leagueWinnerRepository.findPendingTransactions();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pending transactions retrieved successfully",
                "data", pending,
                "count", pending.size()
            ));

        } catch (Exception e) {
            log.error("Error retrieving pending transactions", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error retrieving pending transactions"
            ));
        }
    }

    /**
     * Get all failed transactions
     * GET /api/winners/failed
     * Requires ADMIN role
     */
    @GetMapping("/failed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getFailedTransactions() {
        try {
            List<LeagueWinner> failed = leagueWinnerRepository.findFailedTransactions();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Failed transactions retrieved successfully",
                "data", failed,
                "count", failed.size()
            ));

        } catch (Exception e) {
            log.error("Error retrieving failed transactions", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error retrieving failed transactions"
            ));
        }
    }

    /**
     * Get transaction status by hash
     * GET /api/winners/transactions/{txHash}
     */
    @GetMapping("/transactions/{txHash}")
    public ResponseEntity<?> getTransactionStatus(@PathVariable String txHash) {
        try {
            LeagueWinner winner = leagueWinnerRepository.findBySolanaTransactionHash(txHash)
                .orElse(null);

            if (winner == null) {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Transaction not found: " + txHash
                ));
            }

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Transaction status retrieved successfully",
                "data", Map.of(
                    "transactionHash", winner.getSolanaTransactionHash(),
                    "status", winner.getTransactionStatus(),
                    "leagueName", winner.getLeague().getName(),
                    "winner", winner.getWinner().getUsername(),
                    "confirmedAt", winner.getConfirmedAt()
                )
            ));

        } catch (Exception e) {
            log.error("Error retrieving transaction status", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error retrieving transaction status"
            ));
        }
    }

    /**
     * Manually announce winner for a league (for testing/admin)
     * POST /api/winners/leagues/{leagueId}/announce
     * Requires ADMIN role
     */
    @PostMapping("/leagues/{leagueId}/announce")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> announceWinner(@PathVariable Long leagueId) {
        try {
            log.info("Admin requested manual winner announcement for league: {}", leagueId);
            winnerAnnouncementScheduler.announceWinnerForLeague(leagueId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Winner announcement triggered successfully"
            ));

        } catch (Exception e) {
            log.error("Error announcing winner", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Get winner statistics
     * GET /api/winners/stats
     * Requires ADMIN role
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getWinnerStatistics() {
        try {
            long totalWinners = leagueWinnerRepository.count();
            long confirmedTransactions = leagueWinnerRepository.countByTransactionStatus(TransactionStatus.CONFIRMED);
            long pendingTransactions = leagueWinnerRepository.findPendingTransactions().size();
            long failedTransactions = leagueWinnerRepository.findFailedTransactions().size();

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalWinners", totalWinners);
            stats.put("confirmedTransactions", confirmedTransactions);
            stats.put("pendingTransactions", pendingTransactions);
            stats.put("failedTransactions", failedTransactions);
            stats.put("successRate", totalWinners > 0 ? (double) confirmedTransactions / totalWinners * 100 : 0);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Winner statistics retrieved successfully",
                "data", stats
            ));

        } catch (Exception e) {
            log.error("Error retrieving winner statistics", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Error retrieving winner statistics"
            ));
        }
    }

    /**
     * Health check endpoint for oracle
     * GET /api/winners/oracle/health
     */
    @GetMapping("/oracle/health")
    public ResponseEntity<?> getOracleHealth() {
        return ResponseEntity.ok(Map.of(
            "success", true,
            "message", "Oracle service is healthy",
            "status", "ACTIVE",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
