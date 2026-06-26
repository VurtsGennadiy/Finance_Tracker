package ru.vgd.tracker.dal.entity;

import lombok.Getter;

@Getter
public enum CardType {
    DEBIT("Дебетовая"),
    CREDIT("Кредитная");

    private final String displayName;

    CardType(final String displayName) {
        this.displayName = displayName;
    }
}
