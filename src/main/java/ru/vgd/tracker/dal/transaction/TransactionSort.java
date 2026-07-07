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
    CREATED_AT_ASC(Sort.by("createdAt").ascending()),
    CREATED_AT_DESC(Sort.by("createdAt").descending());

    private final Sort sortValue;
}
