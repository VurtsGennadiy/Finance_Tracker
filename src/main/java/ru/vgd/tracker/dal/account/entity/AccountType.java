package ru.vgd.tracker.dal.account.entity;

import lombok.Getter;

@Getter
public enum AccountType {
    BANK("Банковский"),
    CARD("Карта"),
    CASH("Наличные");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }
}
