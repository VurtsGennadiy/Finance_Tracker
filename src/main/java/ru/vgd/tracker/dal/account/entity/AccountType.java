package ru.vgd.tracker.dal.account.entity;

import lombok.Getter;

@Getter
public enum AccountType {
    DEBIT_CARD("Дебетовая карта", false),
    CREDIT_CARD("Кредитная карта", true),
    BANK("Банковский счёт", false),
    CASH("Наличные", false),
    LOAN("Займ", true);

    private final String displayName;
    private final boolean creditAccount;

    AccountType(String displayName, boolean creditAccount) {
        this.displayName = displayName;
        this.creditAccount = creditAccount;
    }
}
