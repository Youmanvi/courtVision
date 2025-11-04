import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { LeagueProvider } from './context/LeagueContext';
import ProtectedRoute from './components/ProtectedRoute';
import LoginPage from './pages/LoginPage';
import RegisterPage from './pages/RegisterPage';
import DashboardPage from './pages/DashboardPage';
import LeaguesListPage from './pages/LeaguesListPage';
import LeagueDetailPage from './pages/LeagueDetailPage';
import './App.css';

function App() {
  return (
    <Router>
      <AuthProvider>
        <LeagueProvider>
          <Routes>
            {/* Public Routes */}
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            {/* Protected Routes */}
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <DashboardPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/leagues"
              element={
                <ProtectedRoute>
                  <LeaguesListPage />
                </ProtectedRoute>
              }
            />

            <Route
              path="/leagues/:leagueId"
              element={
                <ProtectedRoute>
                  <LeagueDetailPage />
                </ProtectedRoute>
              }
            />

            {/* Redirect root to dashboard or login based on auth status */}
            <Route path="/" element={<Navigate to="/dashboard" replace />} />

            {/* Catch-all route */}
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </LeagueProvider>
      </AuthProvider>
    </Router>
  );
}

export default App;
