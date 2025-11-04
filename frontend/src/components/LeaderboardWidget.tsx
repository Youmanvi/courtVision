import React, { useEffect, useState } from 'react';
import leaderboardService, { ScoreData } from '../services/leaderboardService';

interface LeaderboardWidgetProps {
  leagueId: number;
  leagueName?: string;
  maxRows?: number;
  currentUserId?: number;
  onLeaderboardClick?: () => void;
}

/**
 * Compact leaderboard widget for embedding in dashboards and league pages
 * Shows top N scores with real-time updates via WebSocket
 */
const LeaderboardWidget: React.FC<LeaderboardWidgetProps> = ({
  leagueId,
  leagueName,
  maxRows = 5,
  currentUserId,
  onLeaderboardClick,
}) => {
  const [scores, setScores] = useState<ScoreData[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const connectAndSubscribe = async () => {
      try {
        if (!leaderboardService.isConnected()) {
          await leaderboardService.connect();
        }

        leaderboardService.subscribeToLeaderboard(leagueId, (message) => {
          setScores(message.scores.slice(0, maxRows));
          setIsLoading(false);
        });
      } catch (error) {
        console.error('Failed to connect to leaderboard:', error);
        setIsLoading(false);
      }
    };

    connectAndSubscribe();

    return () => {
      leaderboardService.unsubscribeFromLeaderboard(leagueId);
    };
  }, [leagueId, maxRows]);

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-md p-4">
        <div className="h-32 flex items-center justify-center">
          <p className="text-gray-500">Loading leaderboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200">
      {/* Widget Header */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-4">
        <h3 className="text-white font-bold text-lg">
          {leagueName || `League ${leagueId}`}
        </h3>
        <p className="text-blue-100 text-xs">Top {maxRows} Scores</p>
      </div>

      {/* Top Score Highlight */}
      {scores.length > 0 && (
        <div className="bg-gradient-to-r from-yellow-50 to-orange-50 p-4 border-b border-yellow-200">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-yellow-700 text-xs font-semibold">LEAGUE LEADER</p>
              <p className="text-gray-900 font-bold text-lg">{scores[0].username}</p>
            </div>
            <div className="text-right">
              <p className="text-gray-600 text-xs">Total Score</p>
              <p className="text-3xl font-bold text-yellow-600">{scores[0].total_score.toFixed(0)}</p>
            </div>
          </div>
        </div>
      )}

      {/* Score List */}
      <div className="divide-y divide-gray-200">
        {scores.length > 0 ? (
          scores.map((score, index) => (
            <div
              key={`${score.user_id}-${index}`}
              className={`px-6 py-3 flex items-center justify-between hover:bg-gray-50 transition-colors ${
                currentUserId === score.user_id ? 'bg-blue-50' : ''
              }`}
            >
              <div className="flex items-center gap-3 flex-1">
                <div className="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center font-bold text-sm">
                  {index + 1}
                </div>
                <span className="text-gray-900 font-medium truncate">
                  {score.username}
                  {currentUserId === score.user_id && (
                    <span className="ml-2 text-xs bg-blue-100 text-blue-700 px-2 py-1 rounded">
                      You
                    </span>
                  )}
                </span>
              </div>
              <div className="text-right ml-4">
                <p className="font-bold text-gray-900">{score.total_score.toFixed(1)}</p>
                <p className="text-xs text-gray-600">{score.players_evaluated} players</p>
              </div>
            </div>
          ))
        ) : (
          <div className="px-6 py-8 text-center text-gray-500">
            <p>No scores yet</p>
          </div>
        )}
      </div>

      {/* Widget Footer */}
      {onLeaderboardClick && (
        <div className="px-6 py-3 bg-gray-50 border-t border-gray-200">
          <button
            onClick={onLeaderboardClick}
            className="w-full text-center text-blue-600 hover:text-blue-700 font-semibold text-sm py-2 transition-colors"
          >
            View Full Leaderboard →
          </button>
        </div>
      )}
    </div>
  );
};

export default LeaderboardWidget;
