package ru.vgd.tracker.dal.account.entity;

import lombok.Getter;

@Getter
public enum AccountType {
    CASH("Наличные", false),
    BANK("Банковский счёт", false),
    DEBIT_CARD("Дебетовая карта", false),
    CREDIT_CARD("Кредитная карта", true),
    LOAN("Займ", true);

    private final String displayName;
    private final boolean creditAccount;

    AccountType(String displayName, boolean creditAccount) {
        this.displayName = displayName;
        this.creditAccount = creditAccount;
    }
}
