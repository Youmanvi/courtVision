/**
 * Secure Token Storage Service
 * Handles JWT token storage and retrieval with security best practices
 */

export interface TokenData {
  accessToken: string;
  tokenType: string;
  username: string;
  walletAddress: string;
  walletVerified: boolean;
  userId: number;
  expiresIn?: number;
}

const TOKEN_KEY = 'courtvision_auth_token';
const USER_DATA_KEY = 'courtvision_user_data';
const WALLET_KEY = 'courtvision_wallet';

export class TokenService {
  /**
   * Decode JWT token payload (without verification)
   * WARNING: This does not verify signature - only for reading claims
   */
  private static decodeToken(token: string): any {
    try {
      const base64Url = token.split('.')[1];
      const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
      const jsonPayload = decodeURIComponent(
        atob(base64)
          .split('')
          .map((c) => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
          .join('')
      );
      return JSON.parse(jsonPayload);
    } catch (error) {
      console.error('Failed to decode token:', error);
      return null;
    }
  }

  /**
   * Check if token is expired
   */
  private static isTokenExpired(token: string): boolean {
    const decoded = this.decodeToken(token);
    if (!decoded || !decoded.exp) {
      return true;
    }
    // exp is in seconds, convert to milliseconds and add 60 second buffer
    const expirationTime = decoded.exp * 1000 - 60000;
    return Date.now() > expirationTime;
  }

  /**
   * Store JWT token securely
   * Using httpOnly cookies would be better for production
   * For now, using sessionStorage for sensitive data (cleared on browser close)
   */
  static setToken(tokenData: TokenData): void {
    try {
      // Store token in sessionStorage (cleared when browser closes)
      sessionStorage.setItem(TOKEN_KEY, tokenData.accessToken);

      // Store user metadata separately (does not include token)
      const userMetadata = {
        username: tokenData.username,
        userId: tokenData.userId,
        walletAddress: tokenData.walletAddress,
        walletVerified: tokenData.walletVerified,
      };
      localStorage.setItem(USER_DATA_KEY, JSON.stringify(userMetadata));

      // Store wallet address separately for quick access
      localStorage.setItem(WALLET_KEY, tokenData.walletAddress);
    } catch (error) {
      console.error('Failed to store token:', error);
      throw new Error('Failed to store authentication token');
    }
  }

  /**
   * Get JWT token
   * Returns null if token is expired or not found
   */
  static getToken(): string | null {
    try {
      const token = sessionStorage.getItem(TOKEN_KEY);

      if (!token) {
        return null;
      }

      // Check if token is expired
      if (this.isTokenExpired(token)) {
        this.clearToken();
        return null;
      }

      return token;
    } catch (error) {
      console.error('Failed to retrieve token:', error);
      return null;
    }
  }

  /**
   * Get user metadata (does not expose token)
   */
  static getUserData() {
    try {
      const data = localStorage.getItem(USER_DATA_KEY);
      return data ? JSON.parse(data) : null;
    } catch (error) {
      console.error('Failed to retrieve user data:', error);
      return null;
    }
  }

  /**
   * Get stored wallet address
   */
  static getWalletAddress(): string | null {
    try {
      return localStorage.getItem(WALLET_KEY);
    } catch (error) {
      console.error('Failed to retrieve wallet address:', error);
      return null;
    }
  }

  /**
   * Check if user is authenticated
   */
  static isAuthenticated(): boolean {
    const token = this.getToken();
    const userData = this.getUserData();
    return !!token && !!userData;
  }

  /**
   * Clear all stored tokens and user data
   */
  static clearToken(): void {
    try {
      sessionStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_DATA_KEY);
      localStorage.removeItem(WALLET_KEY);
    } catch (error) {
      console.error('Failed to clear token:', error);
    }
  }

  /**
   * Get token expiration time (in seconds)
   */
  static getTokenExpiration(): number | null {
    try {
      const token = sessionStorage.getItem(TOKEN_KEY);
      if (!token) return null;

      const decoded = this.decodeToken(token);
      return decoded?.exp || null;
    } catch (error) {
      console.error('Failed to get token expiration:', error);
      return null;
    }
  }

  /**
   * Get time until token expires (in seconds)
   */
  static getTimeUntilExpiration(): number {
    const expiration = this.getTokenExpiration();
    if (!expiration) return 0;

    const secondsUntilExpiration = expiration - Math.floor(Date.now() / 1000);
    return Math.max(0, secondsUntilExpiration);
  }

  /**
   * Refresh token if it's about to expire
   * Should be called periodically
   */
  static shouldRefreshToken(): boolean {
    const timeUntilExpiration = this.getTimeUntilExpiration();
    // Refresh if less than 5 minutes remaining
    return timeUntilExpiration < 300;
  }
}

export default TokenService;
