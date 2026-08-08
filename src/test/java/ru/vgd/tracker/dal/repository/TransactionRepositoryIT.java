package ru.vgd.tracker.dal.repository;

import lombok.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.vgd.tracker.config.TestcontainersConfig;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.transaction.Category;
import ru.vgd.tracker.dal.transaction.Transaction;
import ru.vgd.tracker.dal.transaction.TransactionRepository;
import ru.vgd.tracker.dal.transaction.TransactionType;
import ru.vgd.tracker.dal.user.UserRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(scripts = {"/sql/insert-user.sql", "/sql/insert-accounts.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
@Sql(scripts = {"/sql/delete-accounts.sql", "/sql/delete-user.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
public class TransactionRepositoryIT {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final TestEntityManager entityManager;

    @ParameterizedTest
    @MethodSource("provideTransactionsData")
    @DisplayName("Сохранение транзакции")
    void saveTransaction(TransactionData data) {
        Transaction transaction = Transaction.builder()
                .account(accountRepository.findById(data.getAccountId()).orElseThrow())
                .amount(data.getAmount())
                .type(data.getType())
                .category(data.getCategory())
                .description(data.getDescription())
                .createdBy(userRepository.findById(data.getUserId()).orElseThrow())
                .relatedTransaction(
                        data.getRelatedTransactionId() != null ? transactionRepository.findById(data.getRelatedTransactionId()).orElseThrow() : null)
                .build();

        Transaction saved = transactionRepository.saveAndFlush(transaction);
        entityManager.clear();

        Optional<Transaction> loadedOp = transactionRepository.findById(saved.getId());
        assertTrue(loadedOp.isPresent());
        Transaction loaded = loadedOp.get();

        assertEquals(0, transaction.getAmount().compareTo(loaded.getAmount()));
        assertEquals(transaction.getType(), loaded.getType());
        assertEquals(transaction.getCategory(), loaded.getCategory());
        assertEquals(transaction.getDescription(), loaded.getDescription());
        assertEquals(transaction.getCreatedAt(), loaded.getCreatedAt());
        assertEquals(transaction.getTransactionDate(), loaded.getTransactionDate());
        assertEquals(transaction.getAccount().getId(), loaded.getAccount().getId());
        assertEquals(transaction.getCreatedBy().getId(), loaded.getCreatedBy().getId());
        assertEquals(transaction.getRelatedTransaction(), loaded.getRelatedTransaction());
    }

    private static Stream<Arguments> provideTransactionsData() {
        Random random = new Random();
        TransactionData initial = TransactionData.builder()
                .accountId(UUID.fromString("5cb417d2-93d8-4a43-b495-f2a5ac44d20b"))
                .userId(UUID.fromString("42567893-05e8-4ea2-8d45-6a99941789fb"))
                .amount(new BigDecimal(random.nextInt()))
                .description("начальный баланс")
                .type(TransactionType.INITIAL)
                .build();

        TransactionData income = TransactionData.builder()
                .accountId(UUID.fromString("5cb417d2-93d8-4a43-b495-f2a5ac44d20b"))
                .userId(UUID.fromString("42567893-05e8-4ea2-8d45-6a99941789fb"))
                .amount(new BigDecimal(random.nextInt()))
                .type(TransactionType.INCOME)
                .category(Category.INCOME_SALARY)
                .description("пополнение зарплата")
                .build();

        TransactionData expense = TransactionData.builder()
                .accountId(UUID.fromString("5cb417d2-93d8-4a43-b495-f2a5ac44d20b"))
                .userId(UUID.fromString("42567893-05e8-4ea2-8d45-6a99941789fb"))
                .amount(new BigDecimal(random.nextInt()))
                .type(TransactionType.EXPENSE)
                .category(Category.EXPENSE_PRODUCT)
                .description("расход продукты")
                .build();

        TransactionData transfer_in = TransactionData.builder()
                .accountId(UUID.fromString("5cb417d2-93d8-4a43-b495-f2a5ac44d20b"))
                .userId(UUID.fromString("42567893-05e8-4ea2-8d45-6a99941789fb"))
                .amount(new BigDecimal(random.nextInt()))
                .type(TransactionType.TRANSFER_IN)
                .description("входящий перевод")
                .build();

        TransactionData transfer_out = TransactionData.builder()
                .accountId(UUID.fromString("886d71ce-de71-4aad-86e4-b8a46f92312d"))
                .userId(UUID.fromString("42567893-05e8-4ea2-8d45-6a99941789fb"))
                .amount(new BigDecimal(random.nextInt()))
                .type(TransactionType.TRANSFER_OUT)
                .description("исходящий перевод")
                .build();


        return Stream.of(
                Arguments.of(initial),
                Arguments.of(income),
                Arguments.of(expense),
                Arguments.of(transfer_in),
                Arguments.of(transfer_out)
        );
    }

    @Test
    @DisplayName("Сохранение связанных транзакций перевода")
    void saveRelatedTransactions() {
        Transaction outTransaction = Transaction.builder()
                .account(accountRepository.findById(UUID.fromString("5cb417d2-93d8-4a43-b495-f2a5ac44d20b")).orElseThrow())
                .amount(BigDecimal.TEN)
                .type(TransactionType.TRANSFER_OUT)
                .createdBy(userRepository.findById(UUID.fromString("42567893-05e8-4ea2-8d45-6a99941789fb")).orElseThrow())
                .build();

        Transaction inTransaction = Transaction.builder()
                .account(accountRepository.findById(UUID.fromString("886d71ce-de71-4aad-86e4-b8a46f92312d")).orElseThrow())
                .amount(BigDecimal.TEN)
                .type(TransactionType.TRANSFER_IN)
                .createdBy(userRepository.findById(UUID.fromString("42567893-05e8-4ea2-8d45-6a99941789fb")).orElseThrow())
                .build();

        outTransaction.setRelatedTransaction(inTransaction);
        inTransaction.setRelatedTransaction(outTransaction);

        transactionRepository.saveAllAndFlush(List.of(outTransaction, inTransaction));
        entityManager.clear();

        Optional<Transaction> outTransactionOp = transactionRepository.findById(outTransaction.getId());
        assertTrue(outTransactionOp.isPresent());
        Transaction loadedOutTransaction = outTransactionOp.get();

        Optional<Transaction> inTransactionOp = transactionRepository.findById(inTransaction.getId());
        assertTrue(inTransactionOp.isPresent());
        Transaction loadedInTransaction = inTransactionOp.get();

        assertEquals(loadedInTransaction, loadedOutTransaction.getRelatedTransaction());
        assertEquals(loadedOutTransaction, loadedInTransaction.getRelatedTransaction());
    }

    @AllArgsConstructor
    @Builder
    @Getter
    @ToString
    private static class TransactionData {
        final UUID accountId;
        final UUID relatedTransactionId;
        final UUID userId;
        final BigDecimal amount;
        final String description;
        final TransactionType type;
        final Category category;
    }
}
