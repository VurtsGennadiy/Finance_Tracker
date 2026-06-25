package ru.vgd.tracker.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vgd.tracker.dal.entity.BankAccount;

public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

}
