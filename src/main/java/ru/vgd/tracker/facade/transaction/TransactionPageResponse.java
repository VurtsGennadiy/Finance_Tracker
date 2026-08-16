package ru.vgd.tracker.facade.transaction;

import org.springframework.data.domain.Page;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.service.dto.transaction.TransactionDto;
import ru.vgd.tracker.service.dto.transaction.TransactionFilter;
import ru.vgd.tracker.service.dto.transaction.TransactionSummaryDto;

import java.util.List;

/**
 * DTO для передачи данных страницы операций
 */
public record TransactionPageResponse (
    Page<TransactionDto> transactions,
    TransactionSummaryDto summary,
    TransactionFilter filter,
    List<AccountDto> accounts
) {}
