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
 * Денежный займ.
 * Положительный баланс - мне должны. Отрицательный баланс - я должен.
 */
@Entity
@Table(name = "accounts_loan")
@DiscriminatorValue("LOAN")
@PrimaryKeyJoinColumn(name = "account_id")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class LoanAccount extends Account {

    @Override
    public boolean isCreditAccount() {
        return true;
    }
}
