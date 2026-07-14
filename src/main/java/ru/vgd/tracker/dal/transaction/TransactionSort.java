package ru.vgd.tracker.dal.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

/**
 * Перечисление возможных параметров сортировки транзакций
 */
@Getter
@RequiredArgsConstructor
public enum TransactionSort {
    DEFAULT(Sort.by("transactionDate").descending()
            .and(Sort.by("createdAt").descending())),

    CREATED_AT_ASC(Sort.by("createdAt").ascending()),
    CREATED_AT_DESC(Sort.by("createdAt").descending()),

    TRANSACTION_DATE_ASC(Sort.by("transactionDate").ascending()),
    TRANSACTION_DATE_DESC(Sort.by("transactionDate").descending());

    private final Sort sortValue;

    public Sort and(TransactionSort other) {
        return this.sortValue.and(other.sortValue);
    }
}
