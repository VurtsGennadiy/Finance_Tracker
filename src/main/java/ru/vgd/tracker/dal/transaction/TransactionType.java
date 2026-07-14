package ru.vgd.tracker.dal.transaction;

import lombok.Getter;

@Getter
public enum TransactionType {
    INCOME("Пополнение"),
    EXPENSE("Расход"),
    TRANSFER_OUT("Перевод (списание)"),
    TRANSFER_IN("Перевод (пополнение)"),
    INITIAL("Начальный баланс");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }
}
