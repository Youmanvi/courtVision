import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import Leaderboard from '../components/Leaderboard';
import { useAuth } from '../context/AuthContext';
import api from '../services/api';

interface League {
  id: number;
  name: string;
  description?: string;
  creatorId?: number;
  createdAt?: string;
}

const LeaderboardPage: React.FC = () => {
  const { leagueId } = useParams<{ leagueId: string }>();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [league, setLeague] = useState<League | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!leagueId) {
      setError('League ID is required');
      return;
    }

    fetchLeague(parseInt(leagueId));
  }, [leagueId]);

  const fetchLeague = async (id: number) => {
    try {
      setIsLoading(true);
      const response = await api.get(`/api/leagues/${id}`);
      if (response.data.success) {
        setLeague(response.data.data);
        setError(null);
      } else {
        setError(response.data.message || 'Failed to fetch league');
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Failed to fetch league';
      setError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-screen bg-gray-50">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-4 border-blue-600"></div>
          <p className="mt-6 text-gray-600 text-lg">Loading league...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      {/* Navigation */}
      <div className="max-w-6xl mx-auto px-4 mb-8">
        <button
          onClick={() => navigate(-1)}
          className="flex items-center gap-2 text-blue-600 hover:text-blue-700 font-semibold transition-colors"
        >
          ← Back
        </button>
      </div>

      {/* Error State */}
      {error && (
        <div className="max-w-6xl mx-auto px-4 mb-6">
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-red-700">
            <h2 className="text-xl font-semibold mb-2">Error Loading Leaderboard</h2>
            <p>{error}</p>
            <button
              onClick={() => leagueId && fetchLeague(parseInt(leagueId))}
              className="mt-4 px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 transition-colors"
            >
              Retry
            </button>
          </div>
        </div>
      )}

      {/* Main Content */}
      {league && (
        <>
          {/* League Header */}
          <div className="bg-white border-b border-gray-200 mb-8">
            <div className="max-w-6xl mx-auto px-4 py-8">
              <h1 className="text-4xl font-bold text-gray-900 mb-2">{league.name}</h1>
              {league.description && (
                <p className="text-gray-600 text-lg">{league.description}</p>
              )}
              <div className="mt-4 flex items-center gap-4 text-sm text-gray-600">
                <span>🔗 League ID: {league.id}</span>
                {league.createdAt && (
                  <span>📅 Created: {new Date(league.createdAt).toLocaleDateString()}</span>
                )}
              </div>
            </div>
          </div>

          {/* Leaderboard Component */}
          <Leaderboard
            leagueId={parseInt(leagueId || '0')}
            leagueName={league.name}
            currentUserId={user?.id}
            autoConnect={true}
          />

          {/* Quick Stats Section */}
          <div className="max-w-6xl mx-auto px-4 mt-12">
            <div className="bg-white rounded-lg shadow-md p-6 border border-gray-200">
              <h3 className="text-xl font-bold text-gray-900 mb-4">About This League</h3>
              <div className="grid grid-cols-2 gap-6">
                <div>
                  <p className="text-gray-600 text-sm font-semibold mb-2">Updates</p>
                  <p className="text-gray-900">
                    Scores are calculated daily and updated in real-time via WebSocket.
                    The API endpoint updates every 60 seconds, so rankings are always current.
                  </p>
                </div>
                <div>
                  <p className="text-gray-600 text-sm font-semibold mb-2">Scoring System</p>
                  <ul className="text-gray-900 text-sm space-y-1">
                    <li>1 point per NBA point</li>
                    <li>1.2 points per rebound</li>
                    <li>1.5 points per assist</li>
                    <li>2.0 points per steal</li>
                    <li>2.0 points per block</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default LeaderboardPage;
