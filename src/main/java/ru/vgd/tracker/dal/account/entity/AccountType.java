package ru.vgd.tracker.dal.account.entity;

import lombok.Getter;

@Getter
public enum AccountType {
    BANK("Банковский"),
    CARD("Карта"),
    CASH("Наличные"),
    LOAN("Займ"),
    CREDIT_CARD("Кредитная карта"),
    DEBIT_CARD("Дебетовая карта");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }
}
