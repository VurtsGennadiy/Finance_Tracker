package ru.vgd.tracker.service;

import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.account.AccountCreateRequest;
import ru.vgd.tracker.service.dto.account.AccountDto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<AccountDto> getUserAccounts(UUID userId);

    Account getAccountById(UUID accountId, UUID userId);

    void createAccount(AccountCreateRequest request, User owner);

    void deleteAccount(UUID accountId, User user);

    /**
     * Изменить кредитный лимит счёта кредитной карты.
     * Изменение лимита компенсируется изменением баланса счёта и отдельной транзакцией.
     *
     * @param accountId идентификатор счёта кредитной карты
     * @param newLimit  новый кредитный лимит, не может быть отрицательным
     * @param user      владелец счёта, инициирующий изменение
     */
    void updateCreditLimit(UUID accountId, BigDecimal newLimit, User user);
}
