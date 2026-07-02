package com.tokenlimiter.mapper;

import com.tokenlimiter.dto.response.BalanceResponse;
import com.tokenlimiter.dto.response.ConsumeCreditsResponse;
import com.tokenlimiter.entity.UserAccount;
import org.springframework.stereotype.Component;

/**
 * Maps account entities to API response DTOs.
 * Prevents JPA entities from leaking outside the service layer.
 */
@Component
public class CreditMapper {

    /**
     * Builds a balance response from an account entity.
     *
     * @param account account entity
     * @return balance DTO
     */
    public BalanceResponse toBalanceResponse(UserAccount account) {
        return new BalanceResponse(
                account.getUserId(),
                account.getTokenBalance(),
                account.getAccountStatus());
    }

    /**
     * Builds a consumption response after a successful deduction.
     *
     * @param account account with the updated balance
     * @return consumption result DTO
     */
    public ConsumeCreditsResponse toConsumeCreditsResponse(UserAccount account) {
        return new ConsumeCreditsResponse(account.getUserId(), account.getTokenBalance(), "SUCCESS");
    }
}
