import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import './DashboardPage.css';

const DashboardPage = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="dashboard-container">
      <header className="dashboard-header">
        <div className="header-content">
          <h1>courtVision</h1>
          <div className="user-info">
            <span>Welcome, {user?.username || 'User'}!</span>
            <button onClick={handleLogout} className="logout-button">
              Logout
            </button>
          </div>
        </div>
      </header>

      <main className="dashboard-main">
        <section className="dashboard-section">
          <h2>Dashboard</h2>
          <div className="dashboard-content">
            <p>Welcome to courtVision! You are now authenticated and can access all protected resources.</p>
            <p>This is a secure dashboard that only authenticated users can see.</p>

            <div className="feature-cards">
              <div className="feature-card">
                <h3>🏀 Leagues</h3>
                <p>Manage your fantasy basketball leagues</p>
              </div>
              <div className="feature-card">
                <h3>👥 Teams</h3>
                <p>Create and manage your teams</p>
              </div>
              <div className="feature-card">
                <h3>📊 Stats</h3>
                <p>Track player statistics and scores</p>
              </div>
              <div className="feature-card">
                <h3>💰 Rewards</h3>
                <p>Earn rewards from on-chain gameplay</p>
              </div>
            </div>
          </div>
        </section>

        <section className="dashboard-section">
          <h2>Your Account</h2>
          <div className="user-details">
            <p><strong>Username:</strong> {user?.username || 'N/A'}</p>
            <p><strong>Status:</strong> Active</p>
            <p><strong>Authentication:</strong> JWT Token</p>
          </div>
        </section>
      </main>
    </div>
  );
};

export default DashboardPage;
