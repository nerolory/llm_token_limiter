package com.tokenlimiter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tokenlimiter.dto.request.ConsumeCreditsRequest;
import com.tokenlimiter.dto.response.BalanceResponse;
import com.tokenlimiter.dto.response.ConsumeCreditsResponse;
import com.tokenlimiter.entity.AccountStatus;
import com.tokenlimiter.entity.OperationType;
import com.tokenlimiter.entity.TokenTransaction;
import com.tokenlimiter.entity.UserAccount;
import com.tokenlimiter.exception.InsufficientCreditsException;
import com.tokenlimiter.exception.UserNotFoundException;
import com.tokenlimiter.mapper.CreditMapper;
import com.tokenlimiter.repository.TokenTransactionRepository;
import com.tokenlimiter.repository.UserAccountRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private TokenTransactionRepository tokenTransactionRepository;

    private CreditService creditService;

    @BeforeEach
    void setUp() {
        creditService = new CreditService(
                userAccountRepository, tokenTransactionRepository, new CreditMapper());
    }

    @Test
    void consumeCredits_shouldDeductTokensAndLogHistory_whenBalanceIsSufficient() {
        UserAccount account = new UserAccount("user-uuid-8888", 200L, AccountStatus.ACTIVE);
        when(userAccountRepository.findByUserIdForUpdate("user-uuid-8888"))
                .thenReturn(Optional.of(account));
        when(userAccountRepository.save(account)).thenReturn(account);

        ConsumeCreditsRequest request = new ConsumeCreditsRequest("user-uuid-8888", 45L);
        ConsumeCreditsResponse response = creditService.consumeCredits(request);

        assertThat(response.userId()).isEqualTo("user-uuid-8888");
        assertThat(response.remainingTokens()).isEqualTo(155L);
        assertThat(response.status()).isEqualTo("SUCCESS");
        assertThat(account.getTokenBalance()).isEqualTo(155L);

        ArgumentCaptor<TokenTransaction> transactionCaptor = ArgumentCaptor.forClass(TokenTransaction.class);
        verify(tokenTransactionRepository).save(transactionCaptor.capture());
        TokenTransaction savedTransaction = transactionCaptor.getValue();
        assertThat(savedTransaction.getUserId()).isEqualTo("user-uuid-8888");
        assertThat(savedTransaction.getTokensDelta()).isEqualTo(-45L);
        assertThat(savedTransaction.getBalanceAfter()).isEqualTo(155L);
        assertThat(savedTransaction.getOperationType()).isEqualTo(OperationType.CONSUME);
    }

    @Test
    void consumeCredits_shouldThrowInsufficientCreditsException_whenBalanceIsTooLow() {
        UserAccount account = new UserAccount("user-uuid-9999", 0L, AccountStatus.ACTIVE);
        when(userAccountRepository.findByUserIdForUpdate("user-uuid-9999"))
                .thenReturn(Optional.of(account));

        ConsumeCreditsRequest request = new ConsumeCreditsRequest("user-uuid-9999", 10L);

        assertThatThrownBy(() -> creditService.consumeCredits(request))
                .isInstanceOf(InsufficientCreditsException.class)
                .satisfies(ex -> {
                    InsufficientCreditsException ice = (InsufficientCreditsException) ex;
                    assertThat(ice.getUserId()).isEqualTo("user-uuid-9999");
                    assertThat(ice.getRequestedTokens()).isEqualTo(10L);
                    assertThat(ice.getAvailableTokens()).isEqualTo(0L);
                });
    }

    @Test
    void consumeCredits_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userAccountRepository.findByUserIdForUpdate("unknown-user"))
                .thenReturn(Optional.empty());

        ConsumeCreditsRequest request = new ConsumeCreditsRequest("unknown-user", 5L);

        assertThatThrownBy(() -> creditService.consumeCredits(request))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("unknown-user");
    }

    @Test
    void getBalance_shouldReturnBalance_whenUserExists() {
        UserAccount account = new UserAccount("user-uuid-8888", 200L, AccountStatus.ACTIVE);
        when(userAccountRepository.findByUserId("user-uuid-8888")).thenReturn(Optional.of(account));

        BalanceResponse response = creditService.getBalance("user-uuid-8888");

        assertThat(response.userId()).isEqualTo("user-uuid-8888");
        assertThat(response.currentBalance()).isEqualTo(200L);
        assertThat(response.accountStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    void getBalance_shouldThrowUserNotFoundException_whenUserDoesNotExist() {
        when(userAccountRepository.findByUserId("missing-user")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> creditService.getBalance("missing-user"))
                .isInstanceOf(UserNotFoundException.class);
    }
}
