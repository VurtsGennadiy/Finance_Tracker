package ru.vgd.tracker.dal.account.entity;

import lombok.Getter;

@Getter
public enum AccountType {
    BANK("Банковский", false),
    CARD("Карта", false),
    CASH("Наличные", false),
    LOAN("Займ", true),
    CREDIT_CARD("Кредитная карта", true),
    DEBIT_CARD("Дебетовая карта", false);

    private final String displayName;
    private final boolean creditAccount;

    AccountType(String displayName, boolean creditAccount) {
        this.displayName = displayName;
        this.creditAccount = creditAccount;
    }
}
