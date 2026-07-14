package ru.vgd.tracker.dev;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.account.entity.BankAccount;
import ru.vgd.tracker.dal.account.entity.CardAccount;
import ru.vgd.tracker.dal.account.entity.CardType;
import ru.vgd.tracker.dal.account.entity.CashAccount;
import ru.vgd.tracker.dal.account.repository.AccountRepository;
import ru.vgd.tracker.dal.user.UserRepository;
import ru.vgd.tracker.dal.user.User;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Заполняет БД данными пользователя superuser
 */
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        final String username = "superuser";

        if (userRepository.existsByUsername(username)) {
            return;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(username + "@email.ru");
        user.setPassword(passwordEncoder.encode("password"));

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

        userRepository.save(user);
        accountRepository.saveAll(Set.of(cash, debit, credit, bank));
    }
}
