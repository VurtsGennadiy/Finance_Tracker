package ru.vgd.tracker.dal.account.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "accounts_card_credit")
@DiscriminatorValue("CREDIT_CARD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CreditCardAccount extends Account {
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "card_number")
    private String cardNumber;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;

    @Override
    public boolean isCreditAccount() {
        return true;
    }
}
