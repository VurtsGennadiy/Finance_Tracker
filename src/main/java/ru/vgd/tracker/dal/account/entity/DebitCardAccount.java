package ru.vgd.tracker.dal.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts_card_debit")
@DiscriminatorValue("DEBIT_CARD")
@Getter
@Setter
@NoArgsConstructor
public class DebitCardAccount extends Account {
    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "card_number")
    private String cardNumber;

    @Override
    boolean isCreditAccount() {
        return false;
    }
}
