import React, { createContext, useState, useContext, useCallback } from 'react';
import axios from 'axios';

const LeagueContext = createContext();

const API_BASE_URL = 'http://localhost:8080/api/leagues';

export const LeagueProvider = ({ children }) => {
  const [leagues, setLeagues] = useState([]);
  const [currentLeague, setCurrentLeague] = useState(null);
  const [leagueMembers, setLeagueMembers] = useState([]);
  const [pendingInvitations, setPendingInvitations] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  /**
   * Fetch all leagues for the current user
   */
  const fetchLeagues = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get(`${API_BASE_URL}`);
      if (response.data.success) {
        setLeagues(response.data.data || []);
      } else {
        setError(response.data.message);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch leagues');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Fetch league details by ID
   */
  const fetchLeague = useCallback(async (leagueId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get(`${API_BASE_URL}/${leagueId}`);
      if (response.data.success) {
        setCurrentLeague(response.data.data);
      } else {
        setError(response.data.message);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch league');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Fetch league members
   */
  const fetchLeagueMembers = useCallback(async (leagueId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get(`${API_BASE_URL}/${leagueId}/members`);
      if (response.data.success) {
        setLeagueMembers(response.data.data || []);
      } else {
        setError(response.data.message);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch members');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Create a new league
   */
  const createLeague = useCallback(async (leagueData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.post(`${API_BASE_URL}`, leagueData);
      if (response.data.success) {
        setLeagues([...leagues, response.data.data]);
        return {
          success: true,
          data: response.data.data,
          message: response.data.message,
        };
      } else {
        setError(response.data.message);
        return {
          success: false,
          message: response.data.message,
        };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to create league';
      setError(errorMsg);
      return {
        success: false,
        message: errorMsg,
      };
    } finally {
      setLoading(false);
    }
  }, [leagues]);

  /**
   * Update an existing league
   */
  const updateLeague = useCallback(async (leagueId, leagueData) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.put(`${API_BASE_URL}/${leagueId}`, leagueData);
      if (response.data.success) {
        setCurrentLeague(response.data.data);
        setLeagues(
          leagues.map((l) => (l.id === leagueId ? response.data.data : l))
        );
        return {
          success: true,
          data: response.data.data,
          message: response.data.message,
        };
      } else {
        setError(response.data.message);
        return {
          success: false,
          message: response.data.message,
        };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to update league';
      setError(errorMsg);
      return {
        success: false,
        message: errorMsg,
      };
    } finally {
      setLoading(false);
    }
  }, [leagues]);

  /**
   * Delete a league
   */
  const deleteLeague = useCallback(
    async (leagueId) => {
      setLoading(true);
      setError(null);
      try {
        const response = await axios.delete(`${API_BASE_URL}/${leagueId}`);
        if (response.data.success) {
          setLeagues(leagues.filter((l) => l.id !== leagueId));
          setCurrentLeague(null);
          return {
            success: true,
            message: response.data.message,
          };
        } else {
          setError(response.data.message);
          return {
            success: false,
            message: response.data.message,
          };
        }
      } catch (err) {
        const errorMsg = err.response?.data?.message || 'Failed to delete league';
        setError(errorMsg);
        return {
          success: false,
          message: errorMsg,
        };
      } finally {
        setLoading(false);
      }
    },
    [leagues]
  );

  /**
   * Invite a player to a league
   */
  const invitePlayer = useCallback(async (leagueId, email) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.post(`${API_BASE_URL}/${leagueId}/invite`, {
        email,
      });
      if (response.data.success) {
        return {
          success: true,
          data: response.data.data,
          message: response.data.message,
        };
      } else {
        setError(response.data.message);
        return {
          success: false,
          message: response.data.message,
        };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to invite player';
      setError(errorMsg);
      return {
        success: false,
        message: errorMsg,
      };
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Join a league using invitation token
   */
  const joinLeague = useCallback(async (token) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.post(`${API_BASE_URL}/join?token=${token}`);
      if (response.data.success) {
        await fetchLeagues();
        return {
          success: true,
          data: response.data.data,
          message: response.data.message,
        };
      } else {
        setError(response.data.message);
        return {
          success: false,
          message: response.data.message,
        };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to join league';
      setError(errorMsg);
      return {
        success: false,
        message: errorMsg,
      };
    } finally {
      setLoading(false);
    }
  }, [fetchLeagues]);

  /**
   * Get pending invitations
   */
  const fetchPendingInvitations = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.get(`${API_BASE_URL}/invitations/pending`);
      if (response.data.success) {
        setPendingInvitations(response.data.data || []);
      } else {
        setError(response.data.message);
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to fetch invitations');
    } finally {
      setLoading(false);
    }
  }, []);

  /**
   * Remove a member from the league (creator only)
   */
  const removeMember = useCallback(async (leagueId, userId) => {
    setLoading(true);
    setError(null);
    try {
      const response = await axios.delete(`${API_BASE_URL}/${leagueId}/members/${userId}`);
      if (response.data.success) {
        // Update league members by removing the deleted member
        setLeagueMembers(leagueMembers.filter((m) => m.userId !== userId));
        return {
          success: true,
          message: response.data.message,
        };
      } else {
        setError(response.data.message);
        return {
          success: false,
          message: response.data.message,
        };
      }
    } catch (err) {
      const errorMsg = err.response?.data?.message || 'Failed to remove member';
      setError(errorMsg);
      return {
        success: false,
        message: errorMsg,
      };
    } finally {
      setLoading(false);
    }
  }, [leagueMembers]);

  /**
   * Clear error
   */
  const clearError = useCallback(() => {
    setError(null);
  }, []);

  const value = {
    // State
    leagues,
    currentLeague,
    leagueMembers,
    pendingInvitations,
    loading,
    error,

    // Actions
    fetchLeagues,
    fetchLeague,
    fetchLeagueMembers,
    createLeague,
    updateLeague,
    deleteLeague,
    invitePlayer,
    joinLeague,
    fetchPendingInvitations,
    removeMember,
    clearError,
  };

  return (
    <LeagueContext.Provider value={value}>{children}</LeagueContext.Provider>
  );
};

/**
 * Custom hook to use LeagueContext
 */
export const useLeague = () => {
  const context = useContext(LeagueContext);
  if (!context) {
    throw new Error('useLeague must be used within a LeagueProvider');
  }
  return context;
};
