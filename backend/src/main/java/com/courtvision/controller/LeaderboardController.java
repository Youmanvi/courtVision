package com.courtvision.controller;

import com.courtvision.dto.LeaderboardMessage;
import com.courtvision.dto.ScoreCalculationDTO;
import com.courtvision.entity.ScoreCalculation;
import com.courtvision.service.ScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

/**
 * WebSocket controller for real-time leaderboard updates
 * Handles client subscriptions and score update broadcasts
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LeaderboardController {

    private final ScoreService scoreService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Handle client subscription to a league's leaderboard
     * Sends initial scores when client connects
     */
    @MessageMapping("/leaderboard/subscribe/{leagueId}")
    public void subscribeToLeaderboard(@DestinationVariable Long leagueId) {
        log.info("Client subscribed to leaderboard for league: {}", leagueId);

        // Get current scores for the league
        List<ScoreCalculation> scores = scoreService.getLeagueScoreboard(leagueId);
        List<ScoreCalculationDTO> scoreDTOs = scores.stream()
                .map(ScoreCalculationDTO::fromEntity)
                .toList();

        // Send initial leaderboard data
        LeaderboardMessage message = LeaderboardMessage.builder()
                .leagueId(leagueId)
                .scores(scoreDTOs)
                .timestamp(LocalDateTime.now())
                .messageType("INITIAL_LOAD")
                .build();

        messagingTemplate.convertAndSend("/topic/leaderboard/" + leagueId, message);
    }

    /**
     * Broadcast score updates to all clients subscribed to a league
     * Called by ScoreUpdateConsumer when scores are updated
     */
    public void broadcastScoreUpdate(Long leagueId, String leagueName, List<ScoreCalculationDTO> scores) {
        log.debug("Broadcasting score update to leaderboard: {} ({})", leagueId, leagueName);

        LeaderboardMessage message = LeaderboardMessage.builder()
                .leagueId(leagueId)
                .leagueName(leagueName)
                .scores(scores)
                .timestamp(LocalDateTime.now())
                .messageType("SCORES_UPDATED")
                .build();

        messagingTemplate.convertAndSend("/topic/leaderboard/" + leagueId, message);
    }
}
