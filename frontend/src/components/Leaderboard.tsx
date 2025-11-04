import React, { useEffect, useState } from 'react';
import leaderboardService, { ScoreData, LeaderboardMessage } from '../services/leaderboardService';
import LeaderboardRow from './LeaderboardRow';

interface LeaderboardProps {
  leagueId: number;
  leagueName?: string;
  currentUserId?: number;
  autoConnect?: boolean;
}

const Leaderboard: React.FC<LeaderboardProps> = ({
  leagueId,
  leagueName,
  currentUserId,
  autoConnect = true,
}) => {
  const [scores, setScores] = useState<ScoreData[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [lastUpdate, setLastUpdate] = useState<Date | null>(null);

  // Connect to WebSocket on mount
  useEffect(() => {
    if (!autoConnect) return;

    const connectAndSubscribe = async () => {
      try {
        setIsLoading(true);
        await leaderboardService.connect();
        setIsConnected(true);

        leaderboardService.subscribeToLeaderboard(leagueId, handleLeaderboardUpdate);
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to connect to leaderboard';
        setError(errorMessage);
        setIsConnected(false);
      } finally {
        setIsLoading(false);
      }
    };

    connectAndSubscribe();

    return () => {
      leaderboardService.unsubscribeFromLeaderboard(leagueId);
    };
  }, [leagueId, autoConnect]);

  const handleLeaderboardUpdate = (message: LeaderboardMessage) => {
    setScores(message.scores);
    setLastUpdate(new Date());
    setError(null);
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
          <p className="mt-4 text-gray-600">Connecting to leaderboard...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="w-full max-w-6xl mx-auto p-4">
      {/* Header */}
      <div className="mb-6">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-3xl font-bold text-gray-900">
              {leagueName || `League ${leagueId}`} Leaderboard
            </h2>
            <p className="text-gray-600 mt-1">Real-time score rankings</p>
          </div>
          <div className="flex items-center gap-2">
            <div
              className={`px-3 py-1 rounded-full text-sm font-semibold ${
                isConnected ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
              }`}
            >
              {isConnected ? '🟢 Connected' : '🔴 Disconnected'}
            </div>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700">
            <p className="font-semibold">Connection Error</p>
            <p className="text-sm">{error}</p>
          </div>
        )}

        {/* Last Update */}
        {lastUpdate && (
          <p className="text-xs text-gray-500 mt-2">
            Last updated: {lastUpdate.toLocaleTimeString()}
          </p>
        )}
      </div>

      {/* Leaderboard Table */}
      <div className="bg-white rounded-lg shadow-md overflow-hidden border border-gray-200">
        {/* Column Headers */}
        <div className="bg-gray-50 border-b border-gray-200 px-4 py-3 font-semibold text-gray-700">
          <div className="flex items-center justify-between">
            <div className="w-16">Rank</div>
            <div className="flex-1">Player</div>
            <div className="w-32 text-right">Avg/Player</div>
            <div className="w-32 text-right">Total Score</div>
            <div className="w-24 text-right">Players</div>
          </div>
        </div>

        {/* Score Rows */}
        {scores.length > 0 ? (
          <div>
            {scores.map((score, index) => (
              <LeaderboardRow
                key={`${score.user_id}-${score.calculated_at}`}
                rank={index + 1}
                score={score}
                isCurrentUser={currentUserId === score.user_id}
              />
            ))}
          </div>
        ) : (
          <div className="text-center py-12 text-gray-500">
            <p className="text-lg">No scores yet</p>
            <p className="text-sm">Scores will appear here once they are calculated</p>
          </div>
        )}
      </div>

      {/* Footer Stats */}
      {scores.length > 0 && (
        <div className="mt-6 grid grid-cols-3 gap-4">
          <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
            <p className="text-gray-600 text-sm">Total Participants</p>
            <p className="text-3xl font-bold text-blue-600">{scores.length}</p>
          </div>
          <div className="bg-green-50 rounded-lg p-4 border border-green-200">
            <p className="text-gray-600 text-sm">Highest Score</p>
            <p className="text-3xl font-bold text-green-600">
              {Math.max(...scores.map((s) => s.total_score)).toFixed(1)}
            </p>
          </div>
          <div className="bg-purple-50 rounded-lg p-4 border border-purple-200">
            <p className="text-gray-600 text-sm">Average Score</p>
            <p className="text-3xl font-bold text-purple-600">
              {(scores.reduce((sum, s) => sum + s.total_score, 0) / scores.length).toFixed(1)}
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default Leaderboard;
