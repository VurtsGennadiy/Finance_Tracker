package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.entity.Account;
import ru.vgd.tracker.dal.entity.BankAccount;
import ru.vgd.tracker.dal.entity.CardAccount;
import ru.vgd.tracker.dal.entity.CardType;
import ru.vgd.tracker.dal.entity.CashAccount;
import ru.vgd.tracker.dal.entity.User;
import ru.vgd.tracker.dal.repository.AccountRepository;
import ru.vgd.tracker.dal.repository.UserRepository;
import ru.vgd.tracker.service.dto.CreateAccountRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    /**
     * Получить все счета пользователя
     */
    @Transactional(readOnly = true)
    public List<Account> getUserAccounts(UUID userId) {
        return accountRepository.findAllByOwnersId(userId);
    }

    /**
     * Получить первого пользователя (для разработки, пока нет аутентификации)
     */
    @Transactional(readOnly = true)
    public User getFirstUser() {
        return userRepository.findAll().stream().findFirst()
                .orElseThrow(() -> new IllegalStateException("В системе нет ни одного пользователя"));
    }

    /**
     * Создать новый счёт и привязать его к пользователю
     */
    @Transactional
    public Account createAccount(CreateAccountRequest request, User owner) {
        Account account = switch (request.getAccountType()) {
            case "BANK" -> createBankAccount(request);
            case "CARD" -> createCardAccount(request);
            case "CASH" -> createCashAccount(request);
            default -> throw new IllegalArgumentException("Неизвестный тип счёта: " + request.getAccountType());
        };

        account.setName(request.getName());
        account.setBalance(request.getBalance() != null ? request.getBalance() : BigDecimal.ZERO);

        owner = userRepository.loadUserWithRolesByUsername(owner.getUsername()).get();
        owner.getAccounts().add(account);
        userRepository.save(owner);

        return account;
    }

    private BankAccount createBankAccount(CreateAccountRequest request) {
        BankAccount account = new BankAccount();
        account.setBankName(request.getBankName());
        account.setAccountNumber(request.getAccountNumber());
        return account;
    }

    private CardAccount createCardAccount(CreateAccountRequest request) {
        CardAccount account = new CardAccount();
        account.setBankName(request.getBankName());
        account.setCardNumber(request.getCardNumber());
        account.setCardType(CardType.valueOf(request.getCardType()));
        account.setCreditLimit(request.getCreditLimit());
        return account;
    }

    private CashAccount createCashAccount(CreateAccountRequest request) {
        return new CashAccount();
    }
}
