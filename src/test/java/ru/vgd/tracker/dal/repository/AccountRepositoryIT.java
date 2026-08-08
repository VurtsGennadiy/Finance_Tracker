package ru.vgd.tracker.dal.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.vgd.tracker.config.TestcontainersConfig;
import ru.vgd.tracker.dal.account.entity.*;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.dal.user.UserRepository;
import ru.vgd.tracker.dal.user.UserRole;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AccountRepositoryIT {

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TestEntityManager entityManager;

    private User user;

    @BeforeEach
    void setData() {
        user = new User();
        user.setUsername("test_user");
        user.setEmail("test_user@mail.ru");
        user.setConfirmedEmail(true);
        user.setPassword("password");
        user.setRoles(Set.of(UserRole.USER));
        userRepository.saveAndFlush(user);
    }

    @Test
    @DisplayName("Сохранение счёта типа 'Наличные'")
    void saveCashAccount() {
        BigDecimal balance = new BigDecimal("1000");
        String name = "cash_account";

        CashAccount account = CashAccount.builder()
                .owners(Set.of(user))
                .balance(balance)
                .name(name)
                .build();
        Account saved = accountRepository.saveAndFlush(account);
        entityManager.clear();

        Optional<Account> loadedOp = accountRepository.findById(saved.getId());
        assertTrue(loadedOp.isPresent());
        Account loaded = loadedOp.get();
        assertEquals(AccountType.CASH, loaded.getAccountType());
        CashAccount cashAccount = (CashAccount) loaded;

        assertEquals(0, balance.compareTo(cashAccount.getBalance()));
        assertEquals(name, cashAccount.getName());
        assertEquals(Set.of(user), cashAccount.getOwners());
    }

    @Test
    @DisplayName("Сохранение счёта типа 'Банковский счёт'")
    void saveBankAccount() {
        BigDecimal balance = new BigDecimal("1000");
        String name = "bank_account";
        String bankName = "bank_name";
        String accountNumber = "000 123 456";

        BankAccount account = BankAccount.builder()
                .owners(Set.of(user))
                .balance(balance)
                .name(name)
                .bankName(bankName)
                .accountNumber(accountNumber)
                .build();
        Account saved = accountRepository.saveAndFlush(account);
        entityManager.clear();

        Optional<Account> loadedOp = accountRepository.findById(saved.getId());
        assertTrue(loadedOp.isPresent());
        Account loaded = loadedOp.get();
        assertEquals(AccountType.BANK, loaded.getAccountType());
        BankAccount bankAccount = (BankAccount) loaded;

        assertEquals(Set.of(user), bankAccount.getOwners());
        assertEquals(0, balance.compareTo(loaded.getBalance()));
        assertEquals(name, bankAccount.getName());
        assertEquals(bankName, bankAccount.getBankName());
        assertEquals(accountNumber, bankAccount.getAccountNumber());
    }

    @Test
    @DisplayName("Сохранение счёта типа 'Дебетовая карта'")
    void saveDebitCardAccount() {
        BigDecimal balance = new BigDecimal("1000");
        String name = "debit_card";
        String bankName = "bank_name";
        String cardNumber = "1836";

        DebitCardAccount account = DebitCardAccount.builder()
                .owners(Set.of(user))
                .balance(balance)
                .name(name)
                .bankName(bankName)
                .cardNumber(cardNumber)
                .build();
        Account saved = accountRepository.saveAndFlush(account);
        entityManager.clear();

        Optional<Account> loadedOp = accountRepository.findById(saved.getId());
        assertTrue(loadedOp.isPresent());
        Account loaded = loadedOp.get();
        assertEquals(AccountType.DEBIT_CARD, loaded.getAccountType());
        DebitCardAccount debitCardAccount = (DebitCardAccount) loaded;

        assertEquals(Set.of(user), debitCardAccount.getOwners());
        assertEquals(0, balance.compareTo(debitCardAccount.getBalance()));
        assertEquals(name, debitCardAccount.getName());
        assertEquals(bankName, debitCardAccount.getBankName());
        assertEquals(cardNumber, debitCardAccount.getCardNumber());
    }

    @Test
    @DisplayName("Сохранение счёта типа 'Кредитная карта'")
    void saveCreditCardAccount() {
        BigDecimal balance = new BigDecimal("1000");
        String name = "credit_card";
        String bankName = "bank_name";
        String cardNumber = "1849";
        BigDecimal creditLimit = new BigDecimal("100000");

        CreditCardAccount account = CreditCardAccount.builder()
                .owners(Set.of(user))
                .balance(balance)
                .name(name)
                .bankName(bankName)
                .cardNumber(cardNumber)
                .creditLimit(creditLimit).build();
        Account saved = accountRepository.saveAndFlush(account);
        entityManager.clear();

        Optional<Account> loadedOp = accountRepository.findById(saved.getId());
        assertTrue(loadedOp.isPresent());
        Account loaded = loadedOp.get();
        assertEquals(AccountType.CREDIT_CARD, loaded.getAccountType());
        CreditCardAccount creditCardAccount = (CreditCardAccount) loaded;

        assertEquals(Set.of(user), creditCardAccount.getOwners());
        assertEquals(0, balance.compareTo(creditCardAccount.getBalance()));
        assertEquals(name, creditCardAccount.getName());
        assertEquals(bankName, creditCardAccount.getBankName());
        assertEquals(cardNumber, creditCardAccount.getCardNumber());
        assertEquals(0, creditLimit.compareTo(creditCardAccount.getCreditLimit()));
    }

    @Test
    @DisplayName("Сохранение счёта типа 'Займ'")
    void saveLoanAccount() {
        BigDecimal balance = new BigDecimal("1000");
        String name = "loan_card";

        LoanAccount account = LoanAccount.builder()
                .owners(Set.of(user))
                .balance(balance)
                .name(name)
                .build();
        Account saved = accountRepository.saveAndFlush(account);
        entityManager.clear();

        Optional<Account> loadedOp = accountRepository.findById(saved.getId());
        assertTrue(loadedOp.isPresent());
        Account loaded = loadedOp.get();
        assertEquals(AccountType.LOAN, loaded.getAccountType());
        LoanAccount loanAccount = (LoanAccount) loaded;

        assertEquals(Set.of(user), loanAccount.getOwners());
        assertEquals(0, balance.compareTo(loanAccount.getBalance()));
        assertEquals(name, loanAccount.getName());
    }
}
