package ru.vgd.tracker.dal.transaction;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    List<Transaction> findByAccountOwnersId(UUID accountId, Limit limit, Sort sort);

    List<Transaction> findByAccountId(UUID accountId, Limit limit, Sort sort);

    /**
     * Поиск по инициатору транзакции.
     * @param userId идентификатор пользователя, совершившего транзакцию.
     */
    Page<Transaction> findByCreatedById(UUID userId, Pageable pageable);

    int countByAccountId(UUID accountId);
}
