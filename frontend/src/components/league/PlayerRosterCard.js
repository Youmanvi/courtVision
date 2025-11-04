import React from 'react';
import './PlayerRosterCard.css';

const PlayerRosterCard = ({
  memberName,
  memberEmail,
  memberRole,
  isCurrentUser,
  roster = [],
  rosterLimit = 5,
}) => {
  const filledSpots = roster.length;
  const emptySpots = rosterLimit - filledSpots;

  return (
    <div className={`roster-card ${isCurrentUser ? 'current-user' : ''}`}>
      <div className="roster-header">
        <div className="member-info">
          <h3 className="member-name">{memberName}</h3>
          <div className="member-meta">
            <span className="member-email">{memberEmail}</span>
            {memberRole === 'OWNER' && (
              <span className="badge badge-owner">League Creator</span>
            )}
            {isCurrentUser && (
              <span className="badge badge-current">Your Team</span>
            )}
          </div>
        </div>
        <div className="roster-stats">
          <div className="stat">
            <span className="stat-value">{filledSpots}</span>
            <span className="stat-label">Players</span>
          </div>
          <div className="stat">
            <span className="stat-value">{emptySpots}</span>
            <span className="stat-label">Slots Open</span>
          </div>
        </div>
      </div>

      <div className="roster-body">
        {/* Display roster players */}
        {roster && roster.length > 0 ? (
          <div className="roster-grid">
            {roster.map((player, index) => (
              <div key={index} className="roster-player">
                <div className="player-number">{index + 1}</div>
                <div className="player-info">
                  <div className="player-name">{player.name || 'TBD'}</div>
                  <div className="player-position">{player.position || '-'}</div>
                </div>
              </div>
            ))}

            {/* Show empty slots */}
            {emptySpots > 0 && (
              <>
                {Array.from({ length: emptySpots }).map((_, index) => (
                  <div key={`empty-${index}`} className="roster-player empty">
                    <div className="player-number">
                      {filledSpots + index + 1}
                    </div>
                    <div className="player-info">
                      <div className="player-name">Empty Slot</div>
                      <div className="player-position">-</div>
                    </div>
                  </div>
                ))}
              </>
            )}
          </div>
        ) : (
          <div className="empty-roster">
            <svg className="empty-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2m0 18c-4.42 0-8-3.58-8-8s3.58-8 8-8 8 3.58 8 8-3.58 8-8 8m3.5-9c.83 0 1.5-.67 1.5-1.5S16.33 8 15.5 8 14 8.67 14 9.5s.67 1.5 1.5 1.5m-7 0c.83 0 1.5-.67 1.5-1.5S9.33 8 8.5 8 7 8.67 7 9.5 7.67 11 8.5 11m3.5 6.5c2.33 0 4.31-1.46 5.11-3.5H6.89c.8 2.04 2.78 3.5 5.11 3.5z" />
            </svg>
            <p>No players selected yet</p>
            <small>Add players to build your roster</small>
          </div>
        )}
      </div>

      {/* Roster footer with actions */}
      <div className="roster-footer">
        {isCurrentUser ? (
          <button className="btn btn-sm btn-primary">Edit Roster</button>
        ) : (
          <div className="roster-view-only">
            <small>
              {filledSpots === rosterLimit ? '✓ Roster Complete' : 'In Progress'}
            </small>
          </div>
        )}
      </div>
    </div>
  );
};

export default PlayerRosterCard;
