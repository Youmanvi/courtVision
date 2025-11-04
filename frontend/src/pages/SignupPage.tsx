import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import SolanaWalletService from '../services/walletService';
import api from '../services/api';

interface SignupFormData {
  username: string;
  email: string;
  password: string;
  confirmPassword: string;
  solanaWallet: string;
}

interface SignupErrors {
  [key: string]: string;
}

const SignupPage: React.FC = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState<SignupFormData>({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    solanaWallet: '',
  });

  const [errors, setErrors] = useState<SignupErrors>({});
  const [isLoading, setIsLoading] = useState(false);
  const [walletConnected, setWalletConnected] = useState(false);
  const [phantomAvailable, setPhantomAvailable] = useState(SolanaWalletService.isPhantomInstalled());
  const [generalError, setGeneralError] = useState<string | null>(null);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);

  // Validate form data
  const validateForm = (): boolean => {
    const newErrors: SignupErrors = {};

    if (!formData.username || formData.username.length < 3) {
      newErrors.username = 'Username must be at least 3 characters';
    }

    if (!formData.email || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'Please enter a valid email';
    }

    if (!formData.password || formData.password.length < 8) {
      newErrors.password = 'Password must be at least 8 characters';
    }

    if (formData.password !== formData.confirmPassword) {
      newErrors.confirmPassword = 'Passwords do not match';
    }

    if (!formData.solanaWallet || !SolanaWalletService.validateWalletAddress(formData.solanaWallet)) {
      newErrors.solanaWallet = 'Invalid Solana wallet address';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  // Connect wallet
  const handleConnectWallet = async () => {
    try {
      setIsLoading(true);
      setGeneralError(null);

      if (!SolanaWalletService.isPhantomInstalled()) {
        setPhantomAvailable(false);
        setGeneralError('Phantom wallet not installed. Please install it first.');
        return;
      }

      const connection = await SolanaWalletService.connectWallet();
      const walletAddress = connection.publicKey.toBase58();

      setFormData((prev) => ({
        ...prev,
        solanaWallet: walletAddress,
      }));
      setWalletConnected(true);
      setSuccessMessage('Wallet connected successfully!');
    } catch (error) {
      setGeneralError(error instanceof Error ? error.message : 'Failed to connect wallet');
    } finally {
      setIsLoading(false);
    }
  };

  // Handle form input change
  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
    // Clear error for this field when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: '',
      }));
    }
  };

  // Handle signup
  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault();
    setGeneralError(null);
    setSuccessMessage(null);

    if (!validateForm()) {
      return;
    }

    try {
      setIsLoading(true);

      // Register user with wallet
      const response = await api.post('/api/auth/register', {
        username: formData.username,
        email: formData.email,
        password: formData.password,
        solanaWallet: formData.solanaWallet,
      });

      if (response.data.success) {
        setSuccessMessage('Registration successful! Redirecting to login...');
        setTimeout(() => {
          navigate('/login');
        }, 2000);
      } else {
        setGeneralError(response.data.message || 'Registration failed');
      }
    } catch (error: any) {
      const errorMessage =
        error.response?.data?.message ||
        error.message ||
        'Registration failed. Please try again.';
      setGeneralError(errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center py-12 px-4">
      <div className="w-full max-w-md bg-white rounded-lg shadow-xl">
        {/* Header */}
        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 px-6 py-8 text-white">
          <h1 className="text-3xl font-bold">Create Account</h1>
          <p className="text-blue-100 mt-2">Join courtVision with your Solana wallet</p>
        </div>

        {/* Form */}
        <form onSubmit={handleSignup} className="p-6 space-y-4">
          {/* General Error */}
          {generalError && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-red-700 text-sm">
              {generalError}
            </div>
          )}

          {/* Success Message */}
          {successMessage && (
            <div className="bg-green-50 border border-green-200 rounded-lg p-4 text-green-700 text-sm">
              {successMessage}
            </div>
          )}

          {/* Username */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Username</label>
            <input
              type="text"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
              placeholder="Choose a username"
              disabled={isLoading}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${
                errors.username
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
            />
            {errors.username && <p className="text-red-500 text-xs mt-1">{errors.username}</p>}
          </div>

          {/* Email */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Email</label>
            <input
              type="email"
              name="email"
              value={formData.email}
              onChange={handleInputChange}
              placeholder="your@email.com"
              disabled={isLoading}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${
                errors.email
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
            />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
          </div>

          {/* Password */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Password</label>
            <input
              type="password"
              name="password"
              value={formData.password}
              onChange={handleInputChange}
              placeholder="At least 8 characters"
              disabled={isLoading}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${
                errors.password
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
            />
            {errors.password && <p className="text-red-500 text-xs mt-1">{errors.password}</p>}
          </div>

          {/* Confirm Password */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Confirm Password</label>
            <input
              type="password"
              name="confirmPassword"
              value={formData.confirmPassword}
              onChange={handleInputChange}
              placeholder="Confirm your password"
              disabled={isLoading}
              className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition ${
                errors.confirmPassword
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300 focus:ring-blue-500'
              }`}
            />
            {errors.confirmPassword && (
              <p className="text-red-500 text-xs mt-1">{errors.confirmPassword}</p>
            )}
          </div>

          {/* Solana Wallet Section */}
          <div className="bg-indigo-50 rounded-lg p-4 border border-indigo-200">
            <div className="flex items-center justify-between mb-3">
              <label className="block text-sm font-medium text-gray-700">Solana Wallet</label>
              {walletConnected && <span className="text-xs bg-green-100 text-green-700 px-2 py-1 rounded">Connected</span>}
            </div>

            {!phantomAvailable && (
              <div className="bg-yellow-50 border border-yellow-200 rounded p-2 mb-3 text-xs text-yellow-700">
                🔗 Phantom wallet not detected.{' '}
                <a
                  href="https://phantom.app/"
                  target="_blank"
                  rel="noopener noreferrer"
                  className="underline font-semibold"
                >
                  Install Phantom
                </a>
              </div>
            )}

            {walletConnected ? (
              <div className="bg-white rounded p-3 border border-indigo-300">
                <p className="text-xs text-gray-600 mb-1">Connected Wallet</p>
                <p className="font-mono text-sm text-gray-900 break-all">{formData.solanaWallet}</p>
              </div>
            ) : (
              <input
                type="text"
                name="solanaWallet"
                value={formData.solanaWallet}
                onChange={handleInputChange}
                placeholder="Paste wallet address or connect Phantom"
                disabled={isLoading}
                className={`w-full px-4 py-2 border rounded-lg focus:outline-none focus:ring-2 transition font-mono text-sm ${
                  errors.solanaWallet
                    ? 'border-red-500 focus:ring-red-500'
                    : 'border-gray-300 focus:ring-blue-500'
                }`}
              />
            )}

            {errors.solanaWallet && (
              <p className="text-red-500 text-xs mt-1">{errors.solanaWallet}</p>
            )}

            {/* Connect Wallet Button */}
            <button
              type="button"
              onClick={handleConnectWallet}
              disabled={isLoading || walletConnected}
              className={`w-full mt-3 px-4 py-2 rounded-lg font-semibold transition ${
                walletConnected
                  ? 'bg-green-100 text-green-700 cursor-default'
                  : 'bg-indigo-600 text-white hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed'
              }`}
            >
              {isLoading && walletConnected === false
                ? 'Connecting...'
                : walletConnected
                  ? '✓ Wallet Connected'
                  : '🔌 Connect Phantom'}
            </button>
          </div>

          {/* Security Info */}
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 text-xs text-blue-800">
            <p className="font-semibold mb-1">🔒 Security Notice</p>
            <ul className="space-y-1 list-disc list-inside">
              <li>Your password is encrypted and never shared</li>
              <li>Wallet address is stored securely in our database</li>
              <li>JWT tokens are signed with your wallet info</li>
              <li>Never share your private keys with anyone</li>
            </ul>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isLoading || !walletConnected}
            className="w-full px-4 py-3 bg-blue-600 text-white font-semibold rounded-lg hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed transition"
          >
            {isLoading ? 'Creating Account...' : 'Create Account'}
          </button>

          {/* Login Link */}
          <p className="text-center text-sm text-gray-600">
            Already have an account?{' '}
            <button
              type="button"
              onClick={() => navigate('/login')}
              className="text-blue-600 hover:text-blue-700 font-semibold"
            >
              Login here
            </button>
          </p>
        </form>
      </div>
    </div>
  );
};

export default SignupPage;
