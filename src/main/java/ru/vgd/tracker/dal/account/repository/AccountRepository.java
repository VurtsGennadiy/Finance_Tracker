package ru.vgd.tracker.dal.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vgd.tracker.dal.account.entity.Account;

import java.util.List;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAllByOwnersId(UUID ownerId);

    boolean existsByOwnersIdAndName(UUID ownerId, String name);
}
