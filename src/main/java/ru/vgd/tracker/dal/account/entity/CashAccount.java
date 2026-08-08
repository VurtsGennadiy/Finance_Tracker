package ru.vgd.tracker.dal.account.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Наличные деньги
 */
@Entity
@Table(name = "accounts_cash")
@DiscriminatorValue("CASH")
@PrimaryKeyJoinColumn(name = "account_id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class CashAccount extends Account {

    @Override
    public boolean isCreditAccount() {
        return false;
    }
}
