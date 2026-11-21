package ru.vgd.tracker.dal.transaction;

import lombok.Getter;

@Getter
public enum TransactionType {
    INCOME("Пополнение"),
    EXPENSE("Расход"),
    TRANSFER_OUT("Перевод (списание)"),
    TRANSFER_IN("Перевод (пополнение)"),
    INITIAL("Начальный баланс"),
    CREDIT_LIMIT_CHANGE("Изменение кредитного лимита");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }
}
