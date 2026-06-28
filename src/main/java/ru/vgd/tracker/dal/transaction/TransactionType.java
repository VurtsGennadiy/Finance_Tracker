package ru.vgd.tracker.dal.transaction;

import lombok.Getter;

@Getter
public enum TransactionType {
    DEPOSIT("Пополнение"),
    EXPENSE("Расход"),
    TRANSFER_OUT("Перевод (списание)"),
    TRANSFER_IN("Перевод (пополнение)");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }
}
