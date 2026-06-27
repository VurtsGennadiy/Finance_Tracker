package ru.vgd.tracker.dal.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Банковская карта. Может быть дебетовой или кредитной
 */
@Entity
@Table(name = "accounts_card")
@DiscriminatorValue("CARD")
@PrimaryKeyJoinColumn(name = "account_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardAccount extends Account {

    @Column(name = "bank_name", nullable = false)
    private String bankName;

    @Column(name = "card_number")
    private String cardNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "card_type", nullable = false)
    private CardType cardType;

    @Column(name = "credit_limit")
    private BigDecimal creditLimit;
}
