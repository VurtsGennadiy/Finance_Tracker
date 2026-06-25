package ru.vgd.tracker.dal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vgd.tracker.dal.entity.CardAccount;

public interface CardAccountRepository extends JpaRepository<CardAccount, Long> {

}
