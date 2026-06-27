package ru.vgd.tracker.dal.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
@AllArgsConstructor
public class BankAccount extends Account {

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "account_number")
    private String accountNumber;
}
