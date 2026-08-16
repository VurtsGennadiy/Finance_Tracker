package ru.vgd.tracker.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.transaction.*;

import java.util.List;
import java.util.UUID;

public interface TransactionService {
    void createIncomeTransaction(TransactionCreateRequest request, User user);

    void createExpenseTransaction(TransactionCreateRequest request, User user);

    void createTransferTransactions(TransferCreateRequest request, User user);

    List<TransactionDto> getAccountLastTransactions(UUID accountId);

    List<TransactionDto> getUserLastTransactions(UUID userId);

    Page<TransactionDto> getTransactions(TransactionFilter filter, Pageable pageable);

    void cancelTransaction(UUID transactionId);

    void cancelLastTransaction(UUID userId);

    void createAccountInitialTransaction(Account account);

    TransactionSummaryDto getSummary(TransactionFilter filter);
}
