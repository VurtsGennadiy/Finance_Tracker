package ru.vgd.tracker;

import jakarta.persistence.EntityManager;
import org.hibernate.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.vgd.tracker.model.BankAccount;
import ru.vgd.tracker.model.CardAccount;
import ru.vgd.tracker.model.CardType;
import ru.vgd.tracker.model.CashAccount;

import java.math.BigDecimal;

@SpringBootApplication
public class FinanceTrackerApplication {
    public static void main(String[] args) {
        var appContext = SpringApplication.run(FinanceTrackerApplication.class, args);

        SessionFactory sessionFactory = appContext.getBean(SessionFactory.class);
        EntityManager em = appContext.getBean(EntityManager.class);

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

        var session = sessionFactory.openSession();
        session.beginTransaction();
        session.persist(cashAccount);
        session.persist(debitCard);
        session.persist(creditCard);
        session.persist(bankAccount);
        session.getTransaction().commit();
    }
}
