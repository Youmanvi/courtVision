import React, { useState } from 'react';
import { useLeague } from '../../context/LeagueContext';
import './CreateLeagueModal.css';

const CreateLeagueModal = ({ isOpen, onClose, onSuccess }) => {
  const { createLeague, loading } = useLeague();
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    maxPlayers: 8,
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: name === 'maxPlayers' ? parseInt(value) : value,
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    // Validation
    if (!formData.name.trim()) {
      setError('League name is required');
      return;
    }

    if (formData.name.length < 3 || formData.name.length > 100) {
      setError('League name must be between 3 and 100 characters');
      return;
    }

    if (formData.maxPlayers < 2 || formData.maxPlayers > 8) {
      setError('League size must be between 2 and 8 players');
      return;
    }

    const result = await createLeague(formData);

    if (result.success) {
      setSuccess('League created successfully!');
      setFormData({ name: '', description: '', maxPlayers: 8 });
      setTimeout(() => {
        onSuccess?.(result.data);
        onClose();
      }, 1500);
    } else {
      setError(result.message);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h2>Create New League</h2>
          <button className="modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <form onSubmit={handleSubmit} className="create-league-form">
          <div className="form-group">
            <label htmlFor="name">League Name *</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="e.g., Championship 2024"
              disabled={loading}
              maxLength="100"
            />
            <small className="char-count">
              {formData.name.length}/100
            </small>
          </div>

          <div className="form-group">
            <label htmlFor="description">Description (Optional)</label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              placeholder="Describe your league rules, scoring, etc."
              disabled={loading}
              rows="4"
              maxLength="500"
            />
            <small className="char-count">
              {formData.description.length}/500
            </small>
          </div>

          <div className="form-group">
            <label htmlFor="maxPlayers">Number of Players *</label>
            <div className="player-select">
              <select
                id="maxPlayers"
                name="maxPlayers"
                value={formData.maxPlayers}
                onChange={handleChange}
                disabled={loading}
              >
                {[2, 3, 4, 5, 6, 7, 8].map((num) => (
                  <option key={num} value={num}>
                    {num} Players
                  </option>
                ))}
              </select>
              <small className="info-text">
                Create a league with 2-8 players
              </small>
            </div>
          </div>

          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <div className="modal-footer">
            <button
              type="button"
              className="btn btn-secondary"
              onClick={onClose}
              disabled={loading}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn btn-primary"
              disabled={loading}
            >
              {loading ? 'Creating...' : 'Create League'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CreateLeagueModal;
