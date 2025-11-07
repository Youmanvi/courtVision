package com.courtvision.entity;

/**
 * Solana Transaction Status Enum
 * Tracks the status of blockchain transactions
 */
public enum TransactionStatus {
    /**
     * Transaction is pending submission to Solana
     */
    PENDING("Pending"),

    /**
     * Transaction submitted to Solana blockchain
     */
    SUBMITTED("Submitted"),

    /**
     * Transaction confirmed on Solana blockchain
     */
    CONFIRMED("Confirmed"),

    /**
     * Transaction failed
     */
    FAILED("Failed"),

    /**
     * Transaction was rejected
     */
    REJECTED("Rejected");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
