package ru.vgd.tracker.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.entity.CashAccount;
import ru.vgd.tracker.dal.account.entity.CreditCardAccount;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.util.mapper.AccountMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

    @Mock
    AccountRepository accountRepository;

    @Mock
    AccountMapper accountMapper;

    @Mock
    TransactionServiceImpl transactionService;

    @InjectMocks
    AccountServiceImpl accountService;

    private final User user = user();

    @Test
    @DisplayName("Увеличение кредитного лимита увеличивает баланс на разницу лимитов")
    void updateCreditLimit_whenNewLimitIsGreater_thenBalanceAndLimitIncreased() {
        CreditCardAccount account = creditCardAccount("50000.00", "100000.00");
        BigDecimal newLimit = new BigDecimal("150000.00");

        stubOwnedAccount(account);

        accountService.updateCreditLimit(account.getId(), newLimit, user);

        assertEquals(0, newLimit.compareTo(account.getCreditLimit()));
        assertEquals(0, new BigDecimal("100000.00").compareTo(account.getBalance()));
        verify(accountRepository).save(account);
        verify(transactionService).createCreditLimitChangeTransaction(
                account, new BigDecimal("50000.00"), user);
    }

    @Test
    @DisplayName("Уменьшение кредитного лимита уменьшает баланс на разницу лимитов")
    void updateCreditLimit_whenNewLimitIsLess_thenBalanceAndLimitDecreased() {
        CreditCardAccount account = creditCardAccount("50000.00", "100000.00");
        BigDecimal newLimit = new BigDecimal("40000.00");

        stubOwnedAccount(account);

        accountService.updateCreditLimit(account.getId(), newLimit, user);

        assertEquals(0, newLimit.compareTo(account.getCreditLimit()));
        assertEquals(0, new BigDecimal("-10000.00").compareTo(account.getBalance()));
        verify(accountRepository).save(account);
        verify(transactionService).createCreditLimitChangeTransaction(
                account, new BigDecimal("-60000.00"), user);
    }

    @Test
    @DisplayName("Если лимит не задан, он считается равным нулю")
    void updateCreditLimit_whenPreviousLimitIsNull_thenPreviousLimitIsZero() {
        CreditCardAccount account = creditCardAccount("0.00", null);
        BigDecimal newLimit = new BigDecimal("50000.00");

        stubOwnedAccount(account);

        accountService.updateCreditLimit(account.getId(), newLimit, user);

        assertEquals(0, newLimit.compareTo(account.getCreditLimit()));
        assertEquals(0, newLimit.compareTo(account.getBalance()));
        verify(transactionService).createCreditLimitChangeTransaction(
                account, newLimit, user);
    }

    @Test
    @DisplayName("Нулевой кредитный лимит допустим и уменьшает баланс на величину лимита")
    void updateCreditLimit_whenNewLimitIsZero_thenLimitAndBalanceDecreased() {
        CreditCardAccount account = creditCardAccount("50000.00", "100000.00");

        stubOwnedAccount(account);

        accountService.updateCreditLimit(account.getId(), BigDecimal.ZERO, user);

        assertEquals(0, BigDecimal.ZERO.compareTo(account.getCreditLimit()));
        assertEquals(0, new BigDecimal("-50000.00").compareTo(account.getBalance()));
        verify(accountRepository).save(account);
        verify(transactionService).createCreditLimitChangeTransaction(
                account, new BigDecimal("-100000.00"), user);
    }

    @Test
    @DisplayName("Отрицательный кредитный лимит недопустим")
    void updateCreditLimit_whenNewLimitIsNegative_thenThrowsIllegalArgumentException() {
        CreditCardAccount account = creditCardAccount("50000.00", "100000.00");

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateCreditLimit(account.getId(), new BigDecimal("-1.00"), user));

        verifyNoInteractions(accountRepository, transactionService);
    }

    @Test
    @DisplayName("Неизменённый кредитный лимит не создаёт компенсационную транзакцию")
    void updateCreditLimit_whenNewLimitIsEqual_thenThrowsIllegalArgumentException() {
        CreditCardAccount account = creditCardAccount("50000.00", "100000.00");

        stubOwnedAccount(account);

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateCreditLimit(account.getId(), new BigDecimal("100000.00"), user));

        assertEquals(0, new BigDecimal("50000.00").compareTo(account.getBalance()));
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Кредитный лимит нельзя изменить для счёта, не являющегося кредитной картой")
    void updateCreditLimit_whenAccountIsNotCreditCard_thenThrowsIllegalArgumentException() {
        CashAccount account = CashAccount.builder()
                .id(UUID.randomUUID())
                .name("Наличные")
                .balance(new BigDecimal("1000.00"))
                .owner(user)
                .build();

        stubOwnedAccount(account);

        assertThrows(IllegalArgumentException.class,
                () -> accountService.updateCreditLimit(account.getId(), new BigDecimal("50000.00"), user));

        assertEquals(0, new BigDecimal("1000.00").compareTo(account.getBalance()));
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Чужой счёт недоступен для изменения кредитного лимита")
    void updateCreditLimit_whenUserDoesNotOwnAccount_thenThrowsAccessDeniedException() {
        CreditCardAccount account = creditCardAccount("50000.00", "100000.00");
        account.setOwner(user("other_user"));

        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));

        assertThrows(AccessDeniedException.class,
                () -> accountService.updateCreditLimit(account.getId(), new BigDecimal("150000.00"), user));

        assertEquals(0, new BigDecimal("50000.00").compareTo(account.getBalance()));
        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionService);
    }

    @Test
    @DisplayName("Несуществующий счёт недоступен для изменения кредитного лимита")
    void updateCreditLimit_whenAccountNotFound_thenThrowsItemNotFoundException() {
        UUID accountId = UUID.randomUUID();
        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ItemNotFoundException.class,
                () -> accountService.updateCreditLimit(accountId, new BigDecimal("150000.00"), user));

        verify(accountRepository, never()).save(any(Account.class));
        verifyNoInteractions(transactionService);
    }

    /**
     * Счёт кредитной карты с указанным балансом и лимитом.
     */
    private CreditCardAccount creditCardAccount(String balance, String creditLimit) {
        return CreditCardAccount.builder()
                .id(UUID.randomUUID())
                .name("Кредитная карта")
                .balance(new BigDecimal(balance))
                .creditLimit(creditLimit != null ? new BigDecimal(creditLimit) : null)
                .owner(user)
                .build();
    }

    /**
     * Подготовка моков: счёт существует.
     */
    private void stubOwnedAccount(Account account) {
        when(accountRepository.findById(account.getId())).thenReturn(Optional.of(account));
    }

    private static User user() {
        return user("test_user");
    }

    private static User user(String username) {
        User result = new User();
        result.setId(UUID.randomUUID());
        result.setUsername(username);
        return result;
    }
}
