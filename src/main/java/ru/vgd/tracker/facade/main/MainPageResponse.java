package ru.vgd.tracker.facade.main;

import lombok.Data;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.service.dto.transaction.TransactionDto;

import java.util.List;

/**
 * DTO для передачи данных главной страницы
 */
@Data
public class MainPageResponse {
    Double totalBalance; // общий баланс
    Double availableBalance; // собственные доступные средства
    Double accountsReceivable; // дебиторская задолженность (сумма, которую должны пользователю)
    Double accountsPayable; // кредиторская задолженность (сумма, которую должен пользователь)

    List<AccountDto> accounts; // список счетов
    List<TransactionDto> recentTransactions; // список недавних транзакций
}
