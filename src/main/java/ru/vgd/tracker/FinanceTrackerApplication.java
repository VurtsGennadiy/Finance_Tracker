package ru.vgd.tracker;

import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.vgd.tracker.dal.entity.*;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class FinanceTrackerApplication {
    public static void main(String[] args) {
        var appContext = SpringApplication.run(FinanceTrackerApplication.class, args);

        SessionFactory sessionFactory = appContext.getBean(SessionFactory.class);
        EntityManager em = appContext.getBean(EntityManager.class);
        var session = sessionFactory.openSession();
        session.beginTransaction();

        User superuser = new User();
        superuser.setUsername("superuser");
        superuser.setEmail("superuser@email.ru");


        CashAccount cashAccount = new CashAccount();
        cashAccount.setName("Cash Account");
        cashAccount.setBalance(new BigDecimal("99.99"));

        CardAccount debitCard = new CardAccount();
        debitCard.setName("debit card");
        debitCard.setCardType(CardType.DEBIT);
        debitCard.setBankName("Tbank");
        debitCard.setAccountNumber("1234567890");

        CardAccount creditCard = new CardAccount();
        creditCard.setName("credit card");
        creditCard.setCardType(CardType.CREDIT);
        creditCard.setBankName("Sber");
        creditCard.setAccountNumber("0987654321");

        BankAccount bankAccount = new BankAccount();
        bankAccount.setName("Bank Account");
        bankAccount.setBalance(new BigDecimal("1000.00"));
        bankAccount.setBankName("VTB");
        bankAccount.setAccountNumber("1111222233334444");

        Set<Account> accounts = new HashSet<>();
        accounts.add(cashAccount);
        accounts.add(debitCard);
        accounts.add(creditCard);
        accounts.add(bankAccount);
        superuser.setAccounts(accounts);

        session.persist(superuser);

/*        session.persist(cashAccount);
        session.persist(debitCard);
        session.persist(creditCard);
        session.persist(bankAccount);*/
        session.getTransaction().commit();
    }
}
