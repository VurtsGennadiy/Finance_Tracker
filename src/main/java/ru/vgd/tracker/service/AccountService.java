package ru.vgd.tracker.service;

import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.service.dto.account.AccountCreateRequest;
import ru.vgd.tracker.service.dto.account.AccountDto;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    List<AccountDto> getUserAccounts(UUID userId);

    Account getAccountById(UUID accountId, UUID userId);

    void createAccount(AccountCreateRequest request, User owner);

    void deleteAccount(UUID accountId, User user);
}
