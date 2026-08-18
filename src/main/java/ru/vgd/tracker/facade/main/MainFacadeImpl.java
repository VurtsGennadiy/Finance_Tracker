package ru.vgd.tracker.facade.main;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vgd.tracker.dal.account.entity.AccountType;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.service.dto.account.CreditCardAccountDto;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainFacadeImpl implements MainFacade {
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Override
    public MainPageResponse getMainPageData(UUID userId) {
        log.trace("Получение данных главной страницы для пользователя id: {}", userId);

        MainPageResponse result = new MainPageResponse();
        double availableBalance = 0d;
        double accountsReceivable = 0d;
        double accountsPayable = 0d;
        double totalBalance = 0d;

        Map<AccountType, List<AccountDto>> accountsMap = new EnumMap<>(AccountType.class);
        List<AccountDto> accounts = accountService.getUserAccounts(userId);
        for (AccountDto account : accounts) {
            double balance = account.getBalance().doubleValue();
            if (account.getAccountType().isCreditAccount()) {
                if (AccountType.CREDIT_CARD == account.getAccountType()) {
                    CreditCardAccountDto creditCardAccountDto = (CreditCardAccountDto) account;
                    balance = balance - creditCardAccountDto.getCreditLimit().doubleValue();
                }

                if (balance < 0) {
                    accountsPayable += balance;
                } else {
                    accountsReceivable += balance;
                }
            } else {
                availableBalance += balance;
            }
            totalBalance += balance;

            accountsMap.computeIfAbsent(account.getAccountType(), t -> new ArrayList<>());
            accountsMap.get(account.getAccountType()).add(account);
        }

        result.setAccounts(accountsMap);
        result.setAvailableBalance(availableBalance);
        result.setAccountsReceivable(accountsReceivable);
        result.setAccountsPayable(accountsPayable);
        result.setTotalBalance(totalBalance);
        result.setRecentTransactions(transactionService.getUserLastTransactions(userId));

        return result;
    }
}
