import React, { useState } from 'react';
import { useLeague } from '../../context/LeagueContext';
import './InvitePlayersModal.css';

const InvitePlayersModal = ({ isOpen, onClose, leagueId, onSuccess }) => {
  const { invitePlayer, loading } = useLeague();
  const [email, setEmail] = useState('');
  const [emails, setEmails] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [inviting, setInviting] = useState(false);

  const isValidEmail = (email) => {
    return email.match(/^[^\s@]+@[^\s@]+\.[^\s@]+$/);
  };

  const handleAddEmail = () => {
    setError('');

    if (!email.trim()) {
      setError('Please enter an email address');
      return;
    }

    if (!isValidEmail(email)) {
      setError('Please enter a valid email address');
      return;
    }

    if (emails.includes(email)) {
      setError('This email has already been added');
      return;
    }

    setEmails([...emails, email]);
    setEmail('');
  };

  const handleRemoveEmail = (emailToRemove) => {
    setEmails(emails.filter((e) => e !== emailToRemove));
  };

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleAddEmail();
    }
  };

  const handleSendInvites = async () => {
    if (emails.length === 0) {
      setError('Please add at least one email address');
      return;
    }

    setInviting(true);
    setError('');
    setSuccess('');

    try {
      let successCount = 0;
      let failedEmails = [];

      for (const emailAddress of emails) {
        const result = await invitePlayer(leagueId, emailAddress);
        if (result.success) {
          successCount++;
        } else {
          failedEmails.push({ email: emailAddress, message: result.message });
        }
      }

      if (failedEmails.length === 0) {
        setSuccess(`Successfully sent ${successCount} invitation(s)!`);
        setEmails([]);
        setTimeout(() => {
          onSuccess?.();
          onClose();
        }, 1500);
      } else if (successCount > 0) {
        setSuccess(
          `Sent ${successCount} invitation(s), but ${failedEmails.length} failed`
        );
        // Remove successful emails from list
        setEmails(failedEmails.map((f) => f.email));
      } else {
        setError(failedEmails[0]?.message || 'Failed to send invitations');
      }
    } catch (err) {
      setError(err.message || 'An error occurred');
    } finally {
      setInviting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Invite Players to League</h2>
          <button className="modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <form onSubmit={(e) => e.preventDefault()} className="invite-form">
          {/* Email Input */}
          <div className="email-input-section">
            <label htmlFor="email">Email Address</label>
            <div className="email-input-group">
              <input
                type="email"
                id="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                onKeyPress={handleKeyPress}
                placeholder="player@example.com"
                disabled={inviting}
              />
              <button
                type="button"
                className="btn btn-add"
                onClick={handleAddEmail}
                disabled={inviting || !email.trim()}
              >
                Add
              </button>
            </div>
          </div>

          {/* Email List */}
          {emails.length > 0 && (
            <div className="email-list">
              <label>Invitations to Send ({emails.length})</label>
              <div className="email-items">
                {emails.map((e, index) => (
                  <div key={index} className="email-item">
                    <span className="email-text">{e}</span>
                    <button
                      type="button"
                      className="btn-remove"
                      onClick={() => handleRemoveEmail(e)}
                      disabled={inviting}
                    >
                      ×
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}

          {/* Messages */}
          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          {/* Footer */}
          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
              disabled={inviting}
            >
              Cancel
            </button>
            <button
              type="button"
              className="btn btn-primary"
              onClick={handleSendInvites}
              disabled={inviting || emails.length === 0 || loading}
            >
              {inviting ? 'Sending...' : `Send ${emails.length} Invite${emails.length !== 1 ? 's' : ''}`}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default InvitePlayersModal;
