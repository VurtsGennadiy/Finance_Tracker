package ru.vgd.tracker.dal.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vgd.tracker.dal.account.entity.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

}
