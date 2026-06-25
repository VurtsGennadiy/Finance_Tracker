package ru.vgd.tracker.dal.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class CashAccount extends Account {
}
