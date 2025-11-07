package com.courtvision.service;

import com.courtvision.entity.LeagueWinner;
import com.courtvision.entity.TransactionStatus;
import com.courtvision.kafka.WinnerAnnouncementEvent;
import com.courtvision.repository.LeagueWinnerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Transaction Confirmation Poller
 * Periodically checks Solana blockchain for transaction confirmation status
 * Updates LeagueWinner records and publishes events when status changes
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionConfirmationPoller {

    private final LeagueWinnerRepository leagueWinnerRepository;
    private final SolanaOracleService solanaOracleService;
    private final KafkaTemplate<String, WinnerAnnouncementEvent> kafkaTemplate;

    /**
     * Poll pending and submitted transactions every 30 seconds
     * Checks status with Solana RPC and updates records accordingly
     */
    @Scheduled(fixedRate = 30000, initialDelay = 10000)
    @Transactional
    public void pollPendingTransactions() {
        try {
            log.debug("Starting transaction confirmation poll");

            // Get all pending transactions
            List<LeagueWinner> pendingWinners = leagueWinnerRepository.findPendingTransactions();

            if (pendingWinners.isEmpty()) {
                log.debug("No pending transactions to check");
                return;
            }

            log.info("Polling {} pending transactions", pendingWinners.size());

            int confirmedCount = 0;
            int failedCount = 0;

            for (LeagueWinner winner : pendingWinners) {
                try {
                    // Skip if no transaction hash
                    if (winner.getSolanaTransactionHash() == null || winner.getSolanaTransactionHash().isEmpty()) {
                        log.debug("Skipping winner {} - no transaction hash", winner.getId());
                        continue;
                    }

                    // Check confirmation status on Solana
                    boolean isConfirmed = solanaOracleService.isTransactionConfirmed(
                        winner.getSolanaTransactionHash()
                    );

                    if (isConfirmed) {
                        // Update to confirmed status
                        winner.setTransactionStatus(TransactionStatus.CONFIRMED);
                        winner.setConfirmedAt(LocalDateTime.now());
                        leagueWinnerRepository.save(winner);

                        log.info("Transaction confirmed - Winner ID: {}, TX: {}, League: {}",
                            winner.getId(), winner.getSolanaTransactionHash(), winner.getLeague().getName());

                        // Publish confirmation event
                        publishConfirmationEvent(winner);
                        confirmedCount++;
                    } else {
                        // Still pending, check if it's been too long
                        checkForTimeout(winner);
                    }

                } catch (Exception e) {
                    log.error("Error checking transaction status for winner: {}", winner.getId(), e);
                    failedCount++;
                }
            }

            log.info("Transaction poll complete - Confirmed: {}, Failed: {}", confirmedCount, failedCount);

        } catch (Exception e) {
            log.error("Error in transaction confirmation poller", e);
        }
    }

    /**
     * Check if a transaction has been pending too long and mark as failed
     * Default timeout: 5 minutes (300 seconds)
     */
    private void checkForTimeout(LeagueWinner winner) {
        try {
            // Check if created more than 5 minutes ago
            LocalDateTime createdTime = winner.getCreatedAt();
            LocalDateTime timeoutTime = createdTime.plusSeconds(300);

            if (LocalDateTime.now().isAfter(timeoutTime)) {
                log.warn("Transaction timeout - Winner ID: {}, TX: {}", winner.getId(), winner.getSolanaTransactionHash());

                // Mark as failed if still pending
                if (winner.getTransactionStatus() == TransactionStatus.PENDING ||
                    winner.getTransactionStatus() == TransactionStatus.SUBMITTED) {
                    winner.setTransactionStatus(TransactionStatus.FAILED);
                    winner.setErrorMessage("Transaction confirmation timeout after 5 minutes");
                    leagueWinnerRepository.save(winner);

                    // Publish failure event
                    publishFailureEvent(winner);
                }
            }

        } catch (Exception e) {
            log.error("Error checking transaction timeout for winner: {}", winner.getId(), e);
        }
    }

    /**
     * Publish transaction confirmed event to Kafka
     */
    private void publishConfirmationEvent(LeagueWinner winner) {
        try {
            WinnerAnnouncementEvent event = WinnerAnnouncementEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TRANSACTION_CONFIRMED")
                .leagueId(winner.getLeague().getId())
                .leagueName(winner.getLeague().getName())
                .winnerId(winner.getWinner().getId())
                .winnerUsername(winner.getWinner().getUsername())
                .solanaWallet(winner.getSolanaWallet())
                .finalScore(winner.getFinalScore())
                .transactionStatus(TransactionStatus.CONFIRMED.toString())
                .announcedAt(winner.getConfirmedAt())
                .build();

            kafkaTemplate.send("league-winners-announced", event.getEventId(), event);
            log.debug("Published transaction confirmed event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Error publishing confirmation event", e);
        }
    }

    /**
     * Publish transaction failure event to Kafka
     */
    private void publishFailureEvent(LeagueWinner winner) {
        try {
            WinnerAnnouncementEvent event = WinnerAnnouncementEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("TRANSACTION_FAILED")
                .leagueId(winner.getLeague().getId())
                .leagueName(winner.getLeague().getName())
                .winnerId(winner.getWinner().getId())
                .winnerUsername(winner.getWinner().getUsername())
                .solanaWallet(winner.getSolanaWallet())
                .finalScore(winner.getFinalScore())
                .transactionStatus(TransactionStatus.FAILED.toString())
                .announcedAt(LocalDateTime.now())
                .build();

            kafkaTemplate.send("league-winners-announced", event.getEventId(), event);
            log.debug("Published transaction failure event: {}", event.getEventId());

        } catch (Exception e) {
            log.error("Error publishing failure event", e);
        }
    }

    /**
     * Get transaction confirmation status for a specific winner
     */
    public TransactionStatus getTransactionStatus(Long winnerId) {
        try {
            LeagueWinner winner = leagueWinnerRepository.findById(winnerId)
                .orElseThrow(() -> new IllegalArgumentException("Winner not found: " + winnerId));

            return winner.getTransactionStatus();

        } catch (Exception e) {
            log.error("Error getting transaction status for winner: {}", winnerId, e);
            return TransactionStatus.FAILED;
        }
    }

    /**
     * Manually retry a failed transaction
     */
    @Transactional
    public boolean retryFailedTransaction(Long winnerId) {
        try {
            LeagueWinner winner = leagueWinnerRepository.findById(winnerId)
                .orElseThrow(() -> new IllegalArgumentException("Winner not found: " + winnerId));

            if (winner.getTransactionStatus() != TransactionStatus.FAILED) {
                log.warn("Cannot retry non-failed transaction: {}", winnerId);
                return false;
            }

            log.info("Retrying failed transaction for winner: {}", winnerId);

            // Reset status to pending for retry
            winner.setTransactionStatus(TransactionStatus.PENDING);
            winner.setErrorMessage(null);
            leagueWinnerRepository.save(winner);

            return true;

        } catch (Exception e) {
            log.error("Error retrying failed transaction", e);
            return false;
        }
    }
}
