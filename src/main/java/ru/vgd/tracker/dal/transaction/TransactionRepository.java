package ru.vgd.tracker.dal.transaction;

import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
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

    /**
     * Подсчёт суммы доходов по счетам за период
     */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.type = 'INCOME'
            AND t.account.id in :accounts
            AND t.transactionDate BETWEEN :from AND :to""")
    BigDecimal getIncomeTransactionsSum(
            @Param("accounts") Collection<UUID> accounts,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /**
     * Подсчёт суммы расходов за период
     */
    @Query("""
            SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
            WHERE t.type = 'EXPENSE'
            AND t.account.id in :accounts
            AND t.transactionDate BETWEEN :from AND :to""")
    BigDecimal getExpenseTransactionsSum(
            @Param("accounts") Collection<UUID> accounts,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
