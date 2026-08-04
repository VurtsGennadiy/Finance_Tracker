package ru.vgd.tracker.dal.account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts_card_credit")
@DiscriminatorValue("CREDIT_CARD")
@Getter
@Setter
@NoArgsConstructor
public class CreditCardAccount extends Account {
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Override
    boolean isCreditAccount() {
        return true;
    }
}
