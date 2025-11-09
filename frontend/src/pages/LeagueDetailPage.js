import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useLeague } from '../context/LeagueContext';
import PlayerRosterCard from '../components/league/PlayerRosterCard';
import InvitePlayersModal from '../components/modals/InvitePlayersModal';
import './LeagueDetailPage.css';

const LeagueDetailPage = () => {
  const { leagueId } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const {
    currentLeague,
    leagueMembers,
    loading,
    error,
    fetchLeague,
    fetchLeagueMembers,
    deleteLeague,
    removeMember,
  } = useLeague();

  const [showInviteModal, setShowInviteModal] = useState(false);
  const [deleteConfirm, setDeleteConfirm] = useState(false);
  const [rosters, setRosters] = useState({});

  useEffect(() => {
    if (leagueId) {
      fetchLeague(leagueId);
      fetchLeagueMembers(leagueId);
    }
  }, [leagueId, fetchLeague, fetchLeagueMembers]);

  // Mock roster data - in production, fetch from backend
  useEffect(() => {
    if (leagueMembers.length > 0) {
      const mockRosters = {};
      leagueMembers.forEach((member) => {
        mockRosters[member.userId] = [
          // Mock players - replace with real data
          { name: 'LeBron James', position: 'F' },
          { name: 'Kevin Durant', position: 'F' },
          { name: 'Giannis Antetokounmpo', position: 'F' },
          { name: 'Luka Doncic', position: 'G' },
          { name: 'Stephen Curry', position: 'G' },
        ];
      });
      setRosters(mockRosters);
    }
  }, [leagueMembers]);

  const isCreator = currentLeague && currentLeague.creatorId === user?.id;

  const handleDeleteLeague = async () => {
    if (deleteConfirm) {
      const result = await deleteLeague(leagueId);
      if (result.success) {
        navigate('/leagues');
      }
    } else {
      setDeleteConfirm(true);
      setTimeout(() => setDeleteConfirm(false), 5000);
    }
  };

  const handleRemoveMember = async (memberId, memberUsername) => {
    if (window.confirm(`Are you sure you want to remove ${memberUsername} from the league?`)) {
      const result = await removeMember(leagueId, memberId);
      if (result.success) {
        // Member list will be updated via the context
      }
    }
  };

  if (loading && !currentLeague) {
    return (
      <div className="league-detail">
        <div className="loading-state">
          <div className="spinner"></div>
          <p>Loading league details...</p>
        </div>
      </div>
    );
  }

  if (!currentLeague) {
    return (
      <div className="league-detail">
        <div className="error-state">
          <p>{error || 'League not found'}</p>
          <button className="btn btn-primary" onClick={() => navigate('/leagues')}>
            Back to Leagues
          </button>
        </div>
      </div>
    );
  }

  const currentUserMember = leagueMembers.find((m) => m.userId === user?.id);

  return (
    <div className="league-detail">
      {/* League Header */}
      <div className="league-header">
        <div className="header-content">
          <div className="header-left">
            <button className="back-btn" onClick={() => navigate('/leagues')}>
              ← Back
            </button>
            <div className="header-info">
              <h1>{currentLeague.name}</h1>
              {currentLeague.description && (
                <p className="league-description">{currentLeague.description}</p>
              )}
              <div className="league-meta">
                <span className="meta-item">
                  <strong>Creator:</strong> {currentLeague.creatorUsername}
                </span>
                <span className="meta-separator">•</span>
                <span className="meta-item">
                  <strong>Members:</strong> {currentLeague.currentMemberCount}/{currentLeague.maxPlayers}
                </span>
                <span className="meta-separator">•</span>
                <span className="meta-item">
                  <strong>Status:</strong>{' '}
                  <span className="status-badge">{currentLeague.status}</span>
                </span>
              </div>
            </div>
          </div>

          {/* Header Actions */}
          <div className="header-actions">
            {isCreator && (
              <>
                <button
                  className="btn btn-secondary"
                  onClick={() => setShowInviteModal(true)}
                  disabled={currentLeague.currentMemberCount >= currentLeague.maxPlayers}
                >
                  + Invite Players
                </button>
                <button
                  className={`btn ${deleteConfirm ? 'btn-danger' : 'btn-secondary'}`}
                  onClick={handleDeleteLeague}
                >
                  {deleteConfirm ? 'Confirm Delete?' : 'Delete League'}
                </button>
              </>
            )}
          </div>
        </div>
      </div>

      {/* League Content */}
      <div className="league-content">
        {/* Rosters Section */}
        <section className="rosters-section">
          <div className="section-header">
            <h2>Rosters ({leagueMembers.length})</h2>
            <p className="section-subtitle">
              Each member has up to 5 roster spots to fill
            </p>
          </div>

          {leagueMembers.length === 0 ? (
            <div className="empty-state">
              <p>No members yet. Start by inviting players!</p>
            </div>
          ) : (
            <div className="rosters-grid">
              {leagueMembers.map((member) => (
                <div key={member.userId} className="roster-item-wrapper">
                  <PlayerRosterCard
                    memberName={member.username}
                    memberEmail={member.email}
                    memberRole={member.role}
                    isCurrentUser={member.userId === user?.id}
                    roster={rosters[member.userId] || []}
                    rosterLimit={5}
                  />
                  {isCreator && member.userId !== user?.id && (
                    <button
                      className="btn btn-small btn-danger"
                      onClick={() => handleRemoveMember(member.userId, member.username)}
                      title="Remove member from league"
                    >
                      Remove
                    </button>
                  )}
                </div>
              ))}
            </div>
          )}
        </section>

        {/* League Stats */}
        <section className="league-stats-section">
          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon">👥</div>
              <div className="stat-content">
                <div className="stat-number">
                  {currentLeague.currentMemberCount}/{currentLeague.maxPlayers}
                </div>
                <div className="stat-title">Members</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon">⏰</div>
              <div className="stat-content">
                <div className="stat-number">
                  {new Date(currentLeague.createdAt).toLocaleDateString()}
                </div>
                <div className="stat-title">Created</div>
              </div>
            </div>

            <div className="stat-card">
              <div className="stat-icon">🎯</div>
              <div className="stat-content">
                <div className="stat-number">
                  {Math.round(
                    (currentLeague.currentMemberCount / currentLeague.maxPlayers) * 100
                  )}%
                </div>
                <div className="stat-title">Full</div>
              </div>
            </div>
          </div>
        </section>
      </div>

      {/* Invite Modal */}
      <InvitePlayersModal
        isOpen={showInviteModal}
        onClose={() => setShowInviteModal(false)}
        leagueId={leagueId}
        onSuccess={() => {
          setShowInviteModal(false);
          fetchLeagueMembers(leagueId);
        }}
      />
    </div>
  );
};

export default LeagueDetailPage;
