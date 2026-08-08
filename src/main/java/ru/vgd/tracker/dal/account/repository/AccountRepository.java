package ru.vgd.tracker.dal.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.vgd.tracker.dal.account.entity.Account;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findAllByOwnersId(UUID ownerId);

    boolean existsByOwnersIdAndName(UUID ownerId, String name);

    @Query(value = "SELECT EXISTS(SELECT 1 FROM account_owners WHERE user_id = :userId AND account_id = :accountId)",
            nativeQuery = true)
    boolean isAccountOwnedBy(@Param("userId") UUID userId, @Param("accountId") UUID accountId);
}
