package ru.vgd.tracker.facade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.vgd.tracker.dal.account.entity.AccountType;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.service.dto.account.CreditCardAccountDto;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MainFacadeImpl implements MainFacade {
    private final AccountService accountService;
    private final TransactionService transactionService;

    @Override
    public MainPageDto getMainPageData(UUID userId) {
        log.trace("Получение данных главной страницы для пользователя id: {}", userId);

        MainPageDto result = new MainPageDto();
        double availableBalance = 0d;
        double accountsReceivable = 0d;
        double accountsPayable = 0d;
        double totalBalance = 0d;

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
        }

        result.setAccounts(accounts);
        result.setAvailableBalance(availableBalance);
        result.setAccountsReceivable(accountsReceivable);
        result.setAccountsPayable(accountsPayable);
        result.setTotalBalance(totalBalance);
        result.setRecentTransactions(transactionService.getUserLastTransactions(userId));

        return result;
    }
}
