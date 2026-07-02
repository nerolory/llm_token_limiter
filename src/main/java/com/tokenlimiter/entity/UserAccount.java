package com.tokenlimiter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

/**
 * User token account.
 * The {@code version} field enables optimistic locking for concurrent updates.
 */
@Entity
@Table(name = "user_accounts")
public class UserAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "token_balance", nullable = false)
    private Long tokenBalance;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private AccountStatus accountStatus;

    @Version
    private Long version;

    protected UserAccount() {
    }

    /**
     * Creates a new account with the given initial balance.
     *
     * @param userId external user identifier
     * @param tokenBalance initial token balance
     * @param accountStatus account status
     */
    public UserAccount(String userId, Long tokenBalance, AccountStatus accountStatus) {
        this.userId = userId;
        this.tokenBalance = tokenBalance;
        this.accountStatus = accountStatus;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Long getTokenBalance() {
        return tokenBalance;
    }

    public void setTokenBalance(Long tokenBalance) {
        this.tokenBalance = tokenBalance;
    }

    public AccountStatus getAccountStatus() {
        return accountStatus;
    }

    public Long getVersion() {
        return version;
    }
}
