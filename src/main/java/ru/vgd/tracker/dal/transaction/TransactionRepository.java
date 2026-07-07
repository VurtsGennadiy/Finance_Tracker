package ru.vgd.tracker.dal.transaction;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    List<Transaction> findAllByAccountIdOrderByCreatedAtDesc(UUID accountId);

    List<Transaction> findByAccountOwnersIdOrderByTransactionDateDesc(UUID accountId, Limit limit);

    Page<Transaction> findByCreatedById(UUID userId, Pageable pageable);
}
