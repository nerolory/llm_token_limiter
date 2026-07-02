package com.tokenlimiter.repository;

import com.tokenlimiter.entity.TokenTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for token balance change history.
 */
public interface TokenTransactionRepository extends JpaRepository<TokenTransaction, Long> {
}
