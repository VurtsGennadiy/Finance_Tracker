package ru.vgd.tracker.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.entity.CashAccount;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.transaction.Transaction;
import ru.vgd.tracker.dal.transaction.TransactionRepository;
import ru.vgd.tracker.dal.transaction.TransactionType;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.util.mapper.TransactionMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    TransactionRepository transactionRepository;

    @Mock
    AccountRepository accountRepository;

    @Mock
    TransactionMapper transactionMapper;

    @InjectMocks
    TransactionServiceImpl transactionService;

    private final UUID userId = UUID.randomUUID();

    @Test
    void cancelTransaction_whenIncome_thenBalanceDecreasedAndTransactionDeleted() {
        Account account = account("500.00");
        Transaction transaction = transaction(account, TransactionType.INCOME, "500.00");

        stubCancellableTransaction(transaction);

        transactionService.cancelTransaction(transaction.getId(), userId);

        assertEquals(new BigDecimal("0.00"), account.getBalance());
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void cancelTransaction_whenExpense_thenBalanceIncreasedAndTransactionDeleted() {
        Account account = account("500.00");
        Transaction transaction = transaction(account, TransactionType.EXPENSE, "500.00");

        stubCancellableTransaction(transaction);

        transactionService.cancelTransaction(transaction.getId(), userId);

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void cancelTransaction_whenTransferOut_thenBothBalancesRestoredAndBothTransactionsDeleted() {
        Account fromAccount = account("700.00");
        Account toAccount = account("300.00");

        Transaction transferOut = transaction(fromAccount, TransactionType.TRANSFER_OUT, "300.00");
        Transaction transferIn = transaction(toAccount, TransactionType.TRANSFER_IN, "300.00");
        transferOut.setRelatedTransaction(transferIn);
        transferIn.setRelatedTransaction(transferOut);

        stubCancellableTransaction(transferOut);

        transactionService.cancelTransaction(transferOut.getId(), userId);

        assertEquals(new BigDecimal("1000.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("0.00"), toAccount.getBalance());
        verify(transactionRepository).delete(transferOut);
        verify(transactionRepository).delete(transferIn);
    }

    @Test
    void cancelTransaction_whenTransferIn_thenBothBalancesRestoredAndBothTransactionsDeleted() {
        Account fromAccount = account("700.00");
        Account toAccount = account("300.00");

        Transaction transferOut = transaction(fromAccount, TransactionType.TRANSFER_OUT, "300.00");
        Transaction transferIn = transaction(toAccount, TransactionType.TRANSFER_IN, "300.00");
        transferOut.setRelatedTransaction(transferIn);
        transferIn.setRelatedTransaction(transferOut);

        stubCancellableTransaction(transferIn);

        transactionService.cancelTransaction(transferIn.getId(), userId);

        assertEquals(new BigDecimal("1000.00"), fromAccount.getBalance());
        assertEquals(new BigDecimal("0.00"), toAccount.getBalance());
        verify(transactionRepository).delete(transferIn);
        verify(transactionRepository).delete(transferOut);
    }

    @Test
    void cancelTransaction_whenTransferWithoutRelatedTransaction_thenOnlyBalanceRestored() {
        Account account = account("700.00");
        Transaction transferOut = transaction(account, TransactionType.TRANSFER_OUT, "300.00");

        stubCancellableTransaction(transferOut);

        transactionService.cancelTransaction(transferOut.getId(), userId);

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        verify(transactionRepository).delete(transferOut);
    }

    @Test
    void cancelTransaction_whenInitialTransaction_thenThrowsAndTransactionNotDeleted() {
        Account account = account("1000.00");
        Transaction initialTransaction = transaction(account, TransactionType.INITIAL, "1000.00");

        stubCancellableTransaction(initialTransaction);

        assertThrows(IllegalStateException.class,
                () -> transactionService.cancelTransaction(initialTransaction.getId(), userId));

        assertEquals(new BigDecimal("1000.00"), account.getBalance());
        verify(transactionRepository, never()).delete(initialTransaction);
    }

    @Test
    void cancelTransaction_whenTransactionNotFound_thenThrowsItemNotFoundException() {
        UUID transactionId = UUID.randomUUID();
        when(transactionRepository.findById(transactionId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> transactionService.cancelTransaction(transactionId, userId));

        verify(transactionRepository, never()).delete(any(Transaction.class));
    }

    @Test
    void cancelTransaction_whenUserDoesNotOwnAccount_thenThrowsAccessDeniedAndBalanceUnchanged() {
        Account account = account("500.00");
        Transaction transaction = transaction(account, TransactionType.INCOME, "500.00");

        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(accountRepository.isAccountOwnedBy(userId, account.getId())).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> transactionService.cancelTransaction(transaction.getId(), userId));

        assertEquals(new BigDecimal("500.00"), account.getBalance());
        verify(transactionRepository, never()).delete(transaction);
    }

    /**
     * Счёт с указанным текущим балансом.
     */
    private Account account(String balance) {
        CashAccount account = new CashAccount();
        account.setId(UUID.randomUUID());
        account.setName("Тестовый счёт");
        account.setBalance(new BigDecimal(balance));
        return account;
    }

    /**
     * Транзакция указанного типа по счёту.
     */
    private Transaction transaction(Account account, TransactionType type, String amount) {
        Transaction transaction = new Transaction();
        transaction.setId(UUID.randomUUID());
        transaction.setAccount(account);
        transaction.setAmount(new BigDecimal(amount));
        transaction.setType(type);
        return transaction;
    }

    /**
     * Подготовка моков для успешной отмены: транзакция существует, счёт принадлежит пользователю.
     */
    private void stubCancellableTransaction(Transaction transaction) {
        when(transactionRepository.findById(transaction.getId())).thenReturn(Optional.of(transaction));
        when(accountRepository.isAccountOwnedBy(userId, transaction.getAccount().getId())).thenReturn(true);
    }
}
