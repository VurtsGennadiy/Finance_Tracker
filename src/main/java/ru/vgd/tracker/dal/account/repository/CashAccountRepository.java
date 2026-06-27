package ru.vgd.tracker.dal.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vgd.tracker.dal.account.entity.CashAccount;

public interface CashAccountRepository extends JpaRepository<CashAccount, Long> {
}
