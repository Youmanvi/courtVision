import { PublicKey, Transaction, SystemProgram, LAMPORTS_PER_SOL } from '@solana/web3.js';

/**
 * Solana Wallet Service
 * Handles wallet connection, validation, and secure storage
 */

export interface WalletConnection {
  publicKey: PublicKey;
  connected: boolean;
}

export class SolanaWalletService {
  /**
   * Check if Phantom wallet is installed
   */
  static isPhantomInstalled(): boolean {
    return window && (window as any).solana && (window as any).solana.isPhantom;
  }

  /**
   * Connect to Phantom wallet
   */
  static async connectWallet(): Promise<WalletConnection> {
    if (!this.isPhantomInstalled()) {
      throw new Error('Phantom wallet not found. Please install Phantom extension.');
    }

    try {
      const phantom = (window as any).solana;
      const result = await phantom.connect();
      return {
        publicKey: result.publicKey,
        connected: true,
      };
    } catch (error) {
      if ((error as any).code === 4001) {
        throw new Error('Wallet connection rejected by user');
      }
      throw new Error('Failed to connect wallet: ' + (error as Error).message);
    }
  }

  /**
   * Disconnect from wallet
   */
  static async disconnectWallet(): Promise<void> {
    const phantom = (window as any).solana;
    if (phantom && phantom.isConnected) {
      await phantom.disconnect();
    }
  }

  /**
   * Sign a message with the wallet
   * Used for wallet verification during signup
   */
  static async signMessage(message: string, publicKey: PublicKey): Promise<string> {
    const phantom = (window as any).solana;
    if (!phantom) {
      throw new Error('Phantom wallet not available');
    }

    try {
      const encodedMessage = new TextEncoder().encode(message);
      const signedMessage = await phantom.signMessage(encodedMessage);
      return Buffer.from(signedMessage.signature).toString('base64');
    } catch (error) {
      throw new Error('Failed to sign message: ' + (error as Error).message);
    }
  }

  /**
   * Validate Solana wallet address format
   * Solana addresses are 44 characters, base58 encoded
   */
  static validateWalletAddress(address: string): boolean {
    if (!address || typeof address !== 'string') {
      return false;
    }

    // Base58 pattern (excludes: 0, O, I, l)
    const base58Pattern = /^[1-9A-HJ-NP-Z]{43,44}$/;
    return base58Pattern.test(address.trim());
  }

  /**
   * Get currently connected wallet address
   */
  static async getConnectedWallet(): Promise<PublicKey | null> {
    const phantom = (window as any).solana;
    if (phantom && phantom.publicKey) {
      return phantom.publicKey;
    }
    return null;
  }

  /**
   * Check if wallet is connected
   */
  static isWalletConnected(): boolean {
    const phantom = (window as any).solana;
    return phantom && phantom.isConnected;
  }
}

export default SolanaWalletService;
