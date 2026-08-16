package ru.vgd.tracker.service.dto.transaction;

import java.math.BigDecimal;

/**
 * DTO для передачи данных о суммарных доходах и расходах за период
 */
public record TransactionSummaryDto(
        BigDecimal totalIncomes,
        BigDecimal totalExpenses
) {}
