package ru.vgd.tracker.dal.account.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Счёт в банке, например накопительный или вклад
 */
@Entity
@Table(name = "accounts_bank")
@DiscriminatorValue("BANK")
@PrimaryKeyJoinColumn(name = "account_id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class BankAccount extends Account {

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;

    @Override
    public boolean isCreditAccount() {
        return false;
    }
}
