package com.tokenlimiter.repository;

import com.tokenlimiter.entity.UserAccount;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for user token accounts.
 */
public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    /**
     * Finds an account by external user identifier.
     *
     * @param userId user identifier
     * @return the account, if present
     */
    Optional<UserAccount> findByUserId(String userId);

    /**
     * Finds an account with a pessimistic write lock for safe consumption.
     * The lock is held until the transaction completes.
     *
     * @param userId user identifier
     * @return the locked account, if present
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserAccount u WHERE u.userId = :userId")
    Optional<UserAccount> findByUserIdForUpdate(@Param("userId") String userId);
}
