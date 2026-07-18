package ru.vgd.tracker.dev;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.account.entity.*;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.user.UserRepository;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.dal.user.UserRole;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Заполняет БД данными для пользователей admin и user
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        createAdminUser();
        createUser();
    }

    private void createUser() {
        final String username = "user";
        final String password = "user";

        log.trace("Проверка существования аккаунта пользователя {}", username);
        if (userRepository.existsByUsername(username)) {
            log.trace("Аккаунт пользователя существует {}", username);
            return;
        }

        log.trace("Создание аккаунта пользователя");
        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@email.ru");
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(UserRole.USER));
        userRepository.save(user);
        log.info("Создан аккаунт пользователя");

        log.trace("Создание счетов для пользователя {}", user);
        CashAccount cash = new CashAccount();
        cash.setName("Наличные");
        cash.setBalance(new BigDecimal("5000.00"));
        cash.setOwners(Set.of(user));

        CardAccount debit = new CardAccount();
        debit.setName("Зарплатная карта");
        debit.setCardType(CardType.DEBIT);
        debit.setBankName("Т-Банк");
        debit.setCardNumber("1234567890");
        debit.setBalance(new BigDecimal("42000.50"));
        debit.setOwners(Set.of(user));

        CardAccount credit = new CardAccount();
        credit.setName("Кредитка");
        credit.setCardType(CardType.CREDIT);
        credit.setBankName("Сбербанк");
        credit.setCardNumber("0987654321");
        credit.setCreditLimit(new BigDecimal("150000.00"));
        credit.setBalance(new BigDecimal("-12500.00"));
        credit.setOwners(Set.of(user));

        BankAccount bank = new BankAccount();
        bank.setName("Накопительный счёт");
        bank.setBankName("ВТБ");
        bank.setAccountNumber("1111222233334444");
        bank.setBalance(new BigDecimal("250000.00"));
        bank.setOwners(Set.of(user));

        Set<Account> accounts = Set.of(cash, debit, credit, bank);
        accountRepository.saveAll(accounts);
        log.info("Создано {} счетов для пользователя {}", accounts.size(), username);
    }

    private void createAdminUser() {
        final String username = "admin";
        final String password = "admin";

        log.trace("Проверка существования аккаунта пользователя {}", username);
        if (userRepository.existsByUsername(username)) {
            log.trace("Аккаунт пользователя ADMIN существует {}", username);
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles(Set.of(UserRole.ADMIN));
        user.setEmail(username + "@email.ru");
        userRepository.save(user);

        log.info("Аккаунт пользователя {} создан", username);
    }
}
