package ru.vgd.tracker.dal.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vgd.tracker.dal.account.entity.Account;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findAllByOwnerId(UUID ownerId);

    boolean existsByOwnerIdAndName(UUID ownerId, String name);

    boolean existsByOwnerIdAndId(UUID ownerId, UUID accountId);
}
