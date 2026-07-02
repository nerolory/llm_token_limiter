package com.tokenlimiter.dto.response;

import com.tokenlimiter.entity.AccountStatus;

/**
 * Current state of a user's token account.
 *
 * @param userId user identifier
 * @param currentBalance available token balance
 * @param accountStatus account status
 */
public record BalanceResponse(
        String userId,
        Long currentBalance,
        AccountStatus accountStatus) {
}
