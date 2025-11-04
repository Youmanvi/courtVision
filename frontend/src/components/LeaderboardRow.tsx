import React from 'react';
import { ScoreData } from '../services/leaderboardService';

interface LeaderboardRowProps {
  rank: number;
  score: ScoreData;
  isCurrentUser?: boolean;
}

const LeaderboardRow: React.FC<LeaderboardRowProps> = ({ rank, score, isCurrentUser }) => {
  const getRankBadgeColor = (rank: number) => {
    if (rank === 1) return 'bg-yellow-400 text-black';
    if (rank === 2) return 'bg-gray-300 text-black';
    if (rank === 3) return 'bg-orange-300 text-black';
    return 'bg-gray-100 text-gray-700';
  };

  const getRankIcon = (rank: number) => {
    if (rank === 1) return '🥇';
    if (rank === 2) return '🥈';
    if (rank === 3) return '🥉';
    return null;
  };

  return (
    <div
      className={`flex items-center justify-between p-4 border-b transition-all ${
        isCurrentUser ? 'bg-blue-50 border-blue-200 font-semibold' : 'border-gray-200 hover:bg-gray-50'
      }`}
    >
      {/* Rank */}
      <div className="flex items-center gap-3 w-16">
        <div className={`px-3 py-1 rounded-full font-bold text-sm ${getRankBadgeColor(rank)}`}>
          {rank}
        </div>
        {getRankIcon(rank) && <span className="text-lg">{getRankIcon(rank)}</span>}
      </div>

      {/* Username */}
      <div className="flex-1 min-w-0">
        <p className="text-gray-900 truncate">
          {score.username}
          {isCurrentUser && <span className="ml-2 text-blue-600 text-xs font-semibold">(You)</span>}
        </p>
      </div>

      {/* Average Score */}
      <div className="w-32 text-right">
        <p className="text-gray-600 text-sm">Avg/Player</p>
        <p className="text-lg font-semibold text-gray-900">{score.average_score.toFixed(1)}</p>
      </div>

      {/* Total Score */}
      <div className="w-32 text-right">
        <p className="text-gray-600 text-sm">Total</p>
        <p className="text-2xl font-bold text-blue-600">{score.total_score.toFixed(1)}</p>
      </div>

      {/* Players Evaluated */}
      <div className="w-24 text-right">
        <p className="text-gray-600 text-sm">Players</p>
        <p className="text-lg font-semibold text-gray-900">{score.players_evaluated}</p>
      </div>
    </div>
  );
};

export default LeaderboardRow;
