import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useLeague } from '../context/LeagueContext';
import CreateLeagueModal from '../components/modals/CreateLeagueModal';
import './LeaguesListPage.css';

const LeaguesListPage = () => {
  const navigate = useNavigate();
  const { leagues, loading, fetchLeagues } = useLeague();
  const [showCreateModal, setShowCreateModal] = useState(false);

  useEffect(() => {
    fetchLeagues();
  }, [fetchLeagues]);

  const handleLeagueClick = (leagueId) => {
    navigate(`/leagues/${leagueId}`);
  };

  const handleCreateSuccess = () => {
    fetchLeagues();
  };

  if (loading && leagues.length === 0) {
    return (
      <div className="leagues-page">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading your leagues...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="leagues-page">
      {/* Header */}
      <div className="page-header">
        <div className="header-content">
          <div className="header-info">
            <h1>My Leagues</h1>
            <p className="header-subtitle">
              Manage your fantasy basketball leagues
            </p>
          </div>
          <button
            className="btn btn-primary btn-lg"
            onClick={() => setShowCreateModal(true)}
          >
            + Create League
          </button>
        </div>
      </div>

      {/* Content */}
      <div className="page-content">
        {leagues.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">🏀</div>
            <h2>No Leagues Yet</h2>
            <p>Create your first league to get started with fantasy basketball</p>
            <button
              className="btn btn-primary btn-lg"
              onClick={() => setShowCreateModal(true)}
            >
              Create Your First League
            </button>
          </div>
        ) : (
          <div className="leagues-grid">
            {leagues.map((league) => (
              <div
                key={league.id}
                className="league-card"
                onClick={() => handleLeagueClick(league.id)}
              >
                {/* Card Header */}
                <div className="card-header">
                  <div className="league-icon">🏀</div>
                  <div className="header-badges">
                    {league.creatorId === league.creatorId && (
                      <span className="badge badge-creator">Owner</span>
                    )}
                  </div>
                </div>

                {/* Card Body */}
                <div className="card-body">
                  <h3 className="league-name">{league.name}</h3>

                  {league.description && (
                    <p className="league-desc">{league.description}</p>
                  )}

                  {/* League Stats */}
                  <div className="card-stats">
                    <div className="stat">
                      <div className="stat-value">
                        {league.currentMemberCount}/{league.maxPlayers}
                      </div>
                      <div className="stat-label">Members</div>
                    </div>
                    <div className="stat divider"></div>
                    <div className="stat">
                      <div className="stat-value">
                        {Math.round(
                          (league.currentMemberCount / league.maxPlayers) * 100
                        )}%
                      </div>
                      <div className="stat-label">Full</div>
                    </div>
                  </div>

                  {/* Progress Bar */}
                  <div className="progress-bar">
                    <div
                      className="progress-fill"
                      style={{
                        width: `${
                          (league.currentMemberCount / league.maxPlayers) * 100
                        }%`,
                      }}
                    ></div>
                  </div>

                  {/* Status */}
                  <div className="card-status">
                    <span
                      className={`status-badge status-${league.status.toLowerCase()}`}
                    >
                      {league.status}
                    </span>
                  </div>
                </div>

                {/* Card Footer */}
                <div className="card-footer">
                  <span className="created-date">
                    Created{' '}
                    {new Date(league.createdAt).toLocaleDateString('en-US', {
                      month: 'short',
                      day: 'numeric',
                    })}
                  </span>
                  <span className="arrow">→</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Create League Modal */}
      <CreateLeagueModal
        isOpen={showCreateModal}
        onClose={() => setShowCreateModal(false)}
        onSuccess={handleCreateSuccess}
      />
    </div>
  );
};

export default LeaguesListPage;
