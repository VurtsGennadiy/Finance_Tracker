package ru.vgd.tracker.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.entity.CreditCardAccount;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.transaction.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface TransactionService {
    void createIncomeTransaction(TransactionCreateRequest request, User user);

    void createExpenseTransaction(TransactionCreateRequest request, User user);

    void createTransferTransactions(TransferCreateRequest request, User user);

    List<TransactionDto> getAccountLastTransactions(UUID accountId);

    List<TransactionDto> getUserLastTransactions(UUID userId);

    Page<TransactionDto> getTransactions(TransactionFilter filter, Pageable pageable);

    void cancelTransaction(UUID transactionId, UUID userId);

    void cancelLastTransaction(UUID userId);

    void createAccountInitialTransaction(Account account);

    /**
     * Сохранить компенсационную транзакцию изменения кредитного лимита.
     * Сумма транзакции равна дельте лимита: положительная при увеличении лимита
     * и отрицательная при его уменьшении.
     *
     * @param account    счёт кредитной карты
     * @param limitDelta разница между новым и предыдущим кредитным лимитом
     * @param user       пользователь, инициировавший изменение
     */
    void createCreditLimitChangeTransaction(CreditCardAccount account, BigDecimal limitDelta, User user);

    TransactionSummaryDto getSummary(TransactionFilter filter);
}
