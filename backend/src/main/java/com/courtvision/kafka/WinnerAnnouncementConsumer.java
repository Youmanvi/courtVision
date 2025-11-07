package com.courtvision.kafka;

import com.courtvision.entity.LeagueWinner;
import com.courtvision.entity.TransactionStatus;
import com.courtvision.repository.LeagueWinnerRepository;
import com.courtvision.service.SolanaOracleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Winner Announcement Consumer
 * Consumes winner announcement events and submits to Solana blockchain
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WinnerAnnouncementConsumer {

    private final LeagueWinnerRepository leagueWinnerRepository;
    private final SolanaOracleService solanaOracleService;

    /**
     * Consume winner announcement event and submit to Solana
     *
     * Topics:
     * - league-winners-announced: Official winner announcements
     */
    @KafkaListener(
        topics = "league-winners-announced",
        groupId = "courtvision-winner-consumer-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeWinnerAnnouncement(WinnerAnnouncementEvent event) {
        try {
            log.info("Received winner announcement: League={}, Winner={}", event.getLeagueId(), event.getWinnerUsername());

            // Validate event
            if (!validateEvent(event)) {
                log.error("Invalid winner announcement event: {}", event.getEventId());
                return;
            }

            // Submit to Solana blockchain
            String transactionHash = solanaOracleService.submitWinnerToBlockchain(
                event.getLeagueId(),
                event.getLeagueName(),
                event.getSolanaWallet(),
                event.getFinalScore()
            );

            // Update LeagueWinner record with transaction info
            if (transactionHash != null) {
                updateWinnerRecord(event, transactionHash, TransactionStatus.SUBMITTED);
                log.info("Successfully submitted winner to Solana. TX: {}", transactionHash);
            } else {
                updateWinnerRecord(event, null, TransactionStatus.FAILED);
                log.error("Failed to submit winner to Solana blockchain");
            }

        } catch (Exception e) {
            log.error("Error processing winner announcement event", e);
        }
    }

    /**
     * Validate winner announcement event
     */
    private boolean validateEvent(WinnerAnnouncementEvent event) {
        if (event.getEventId() == null || event.getEventId().isEmpty()) {
            log.error("Event ID is missing");
            return false;
        }

        if (event.getLeagueId() == null) {
            log.error("League ID is missing");
            return false;
        }

        if (event.getWinnerId() == null) {
            log.error("Winner ID is missing");
            return false;
        }

        if (event.getSolanaWallet() == null || !solanaOracleService.isValidSolanaAddress(event.getSolanaWallet())) {
            log.error("Invalid or missing Solana wallet address: {}", event.getSolanaWallet());
            return false;
        }

        if (event.getFinalScore() == null || event.getFinalScore() < 0) {
            log.error("Invalid final score: {}", event.getFinalScore());
            return false;
        }

        return true;
    }

    /**
     * Update LeagueWinner record with transaction information
     */
    private void updateWinnerRecord(WinnerAnnouncementEvent event, String transactionHash, TransactionStatus status) {
        try {
            // Find existing winner record
            LeagueWinner winner = leagueWinnerRepository
                .findByLeagueIdAndWinnerId(event.getLeagueId(), event.getWinnerId())
                .orElse(null);

            if (winner != null) {
                // Update existing record
                winner.setSolanaTransactionHash(transactionHash);
                winner.setTransactionStatus(status);

                if (status == TransactionStatus.CONFIRMED) {
                    winner.setConfirmedAt(LocalDateTime.now());
                }

                leagueWinnerRepository.save(winner);
                log.debug("Updated winner record: ID={}, Status={}, TX={}", winner.getId(), status, transactionHash);
            }

        } catch (Exception e) {
            log.error("Error updating winner record", e);
        }
    }
}
