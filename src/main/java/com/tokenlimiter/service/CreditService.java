package com.tokenlimiter.service;

import com.tokenlimiter.dto.request.ConsumeCreditsRequest;
import com.tokenlimiter.dto.response.BalanceResponse;
import com.tokenlimiter.dto.response.ConsumeCreditsResponse;
import com.tokenlimiter.entity.OperationType;
import com.tokenlimiter.entity.TokenTransaction;
import com.tokenlimiter.entity.UserAccount;
import com.tokenlimiter.exception.InsufficientCreditsException;
import com.tokenlimiter.exception.UserNotFoundException;
import com.tokenlimiter.mapper.CreditMapper;
import com.tokenlimiter.repository.TokenTransactionRepository;
import com.tokenlimiter.repository.UserAccountRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for token consumption and balance retrieval.
 * All balance mutations run inside a transaction with account-level locking.
 */
@Service
public class CreditService {

    private final UserAccountRepository userAccountRepository;
    private final TokenTransactionRepository tokenTransactionRepository;
    private final CreditMapper creditMapper;

    public CreditService(
            UserAccountRepository userAccountRepository,
            TokenTransactionRepository tokenTransactionRepository,
            CreditMapper creditMapper) {
        this.userAccountRepository = userAccountRepository;
        this.tokenTransactionRepository = tokenTransactionRepository;
        this.creditMapper = creditMapper;
    }

    /**
     * Deducts tokens from the user's balance and records the operation in history.
     * The account is locked for the duration of the transaction to prevent race conditions.
     *
     * @param request consumption request data
     * @return operation result with the remaining balance
     * @throws UserNotFoundException if the user does not exist
     * @throws InsufficientCreditsException if the balance is too low
     */
    @Transactional
    public ConsumeCreditsResponse consumeCredits(ConsumeCreditsRequest request) {
        UserAccount account = userAccountRepository
                .findByUserIdForUpdate(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));

        Long currentBalance = account.getTokenBalance();
        if (currentBalance < request.tokensRequested()) {
            throw new InsufficientCreditsException(
                    request.userId(), request.tokensRequested(), currentBalance);
        }

        long remainingTokens = currentBalance - request.tokensRequested();
        account.setTokenBalance(remainingTokens);
        userAccountRepository.save(account);

        TokenTransaction transaction = new TokenTransaction(
                request.userId(),
                -request.tokensRequested(),
                remainingTokens,
                Instant.now(),
                OperationType.CONSUME);
        tokenTransactionRepository.save(transaction);

        return creditMapper.toConsumeCreditsResponse(account);
    }

    /**
     * Returns the current user balance without modifying any data.
     *
     * @param userId external user identifier
     * @return balance and account status
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String userId) {
        UserAccount account = userAccountRepository
                .findByUserId(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        return creditMapper.toBalanceResponse(account);
    }
}
