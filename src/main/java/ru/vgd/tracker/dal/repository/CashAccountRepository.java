package ru.vgd.tracker.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vgd.tracker.dal.entity.CashAccount;

public interface CashAccountRepository extends JpaRepository<CashAccount, Long> {
}
