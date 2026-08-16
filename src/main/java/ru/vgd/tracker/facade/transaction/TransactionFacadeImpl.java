package ru.vgd.tracker.facade.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.service.dto.transaction.TransactionDto;
import ru.vgd.tracker.service.dto.transaction.TransactionFilter;
import ru.vgd.tracker.service.dto.transaction.TransactionSummaryDto;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionFacadeImpl implements TransactionsFacade {
    private final TransactionService transactionService;
    private final AccountService accountService;

    @Override
    public TransactionPageResponse getTransactionPageData(UUID userId, TransactionFilter filter, Pageable pageable) {
        log.trace("Получение данных для страницы /transactions");

        List<AccountDto> accounts = accountService.getUserAccounts(userId);

        if (filter.getAccounts().isEmpty()) {
            filter.setAccounts(accounts.stream().map(AccountDto::getId).toList());
        }

        Page<TransactionDto> transactions = transactionService.getTransactions(filter, pageable);
        TransactionSummaryDto summary = transactionService.getSummary(filter);

        return new TransactionPageResponse(transactions, summary, filter, accounts);
    }
}
