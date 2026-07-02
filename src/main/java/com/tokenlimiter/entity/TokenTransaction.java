package com.tokenlimiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Record in the token balance change log.
 * Persisted after each successful consumption.
 */
@Entity
@Table(name = "token_transactions")
public class TokenTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    /** Balance change: negative for consumption. */
    @Column(name = "tokens_delta", nullable = false)
    private Long tokensDelta;

    @Column(name = "balance_after", nullable = false)
    private Long balanceAfter;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "operation_type", nullable = false)
    private OperationType operationType;

    protected TokenTransaction() {
    }

    /**
     * Creates a transaction record.
     *
     * @param userId user identifier
     * @param tokensDelta balance change (negative for consumption)
     * @param balanceAfter balance after the operation
     * @param createdAt timestamp of the operation
     * @param operationType operation type
     */
    public TokenTransaction(
            String userId,
            Long tokensDelta,
            Long balanceAfter,
            Instant createdAt,
            OperationType operationType) {
        this.userId = userId;
        this.tokensDelta = tokensDelta;
        this.balanceAfter = balanceAfter;
        this.createdAt = createdAt;
        this.operationType = operationType;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Long getTokensDelta() {
        return tokensDelta;
    }

    public Long getBalanceAfter() {
        return balanceAfter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public OperationType getOperationType() {
        return operationType;
    }
}
