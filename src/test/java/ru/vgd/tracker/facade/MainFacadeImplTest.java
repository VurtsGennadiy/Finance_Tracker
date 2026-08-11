package ru.vgd.tracker.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.account.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MainFacadeImplTest {

    @Mock
    AccountService accountService;

    @Mock
    TransactionService transactionService;

    @InjectMocks
    MainFacadeImpl mainFacadeImpl;

    @Test
    void getMainPageData_whenCreditCardBalanceLessLimit_thenAccountsPayableAdd() {
        BigDecimal creditLimit = new BigDecimal("100000");
        BigDecimal balance = new BigDecimal("10000");

        CreditCardAccountDto creditCardAccountDto = new CreditCardAccountDto();
        creditCardAccountDto.setCreditLimit(creditLimit);
        creditCardAccountDto.setBalance(balance);

        when(accountService.getUserAccounts(Mockito.any())).thenReturn(List.of(creditCardAccountDto));
        var result = mainFacadeImpl.getMainPageData(UUID.randomUUID());

        assertEquals(balance.subtract(creditLimit).doubleValue(), result.getAccountsPayable());
        assertEquals(0d, result.getAccountsReceivable());
        assertEquals(0d, result.getAvailableBalance());
        assertEquals(balance.subtract(creditLimit).doubleValue(), result.totalBalance);
    }

    @Test
    void getMainPageData_whenCreditCardBalanceGreaterLimit_thenAccountsReceivableAdd() {
        BigDecimal creditLimit = new BigDecimal("100000");
        BigDecimal balance = new BigDecimal("110000");

        CreditCardAccountDto creditCardAccountDto = new CreditCardAccountDto();
        creditCardAccountDto.setCreditLimit(creditLimit);
        creditCardAccountDto.setBalance(balance);

        when(accountService.getUserAccounts(Mockito.any())).thenReturn(List.of(creditCardAccountDto));
        var result = mainFacadeImpl.getMainPageData(UUID.randomUUID());

        assertEquals(balance.subtract(creditLimit).doubleValue(), result.getAccountsReceivable());
        assertEquals(0d, result.getAccountsPayable());
        assertEquals(0d, result.getAvailableBalance());
        assertEquals(balance.subtract(creditLimit).doubleValue(), result.totalBalance);
    }

    @Test
    void getMainPageData_whenCreditAccounts_thenCreditAdd() {
        LoanAccountDto loanPayable = new LoanAccountDto(new BigDecimal("-1000"));
        LoanAccountDto loanReceivable = new LoanAccountDto(new BigDecimal("2000"));
        CreditCardAccountDto creditCardPayable = new CreditCardAccountDto(new BigDecimal("7000"), new BigDecimal("10000"));
        CreditCardAccountDto creditCardReceivable = new CreditCardAccountDto(new BigDecimal("14000"), new BigDecimal("10000"));

        double expectedPayable = loanPayable.getBalance()
                .add(creditCardPayable.getBalance())
                .subtract(creditCardPayable.getCreditLimit())
                .doubleValue();

        double expectedReceivable = loanReceivable.getBalance()
                .add(creditCardReceivable.getBalance())
                .subtract(creditCardReceivable.getCreditLimit())
                .doubleValue();

        double expectedTotalBalance = loanPayable.getBalance()
                .add(loanReceivable.getBalance())
                .add(creditCardPayable.getBalance())
                .add(creditCardReceivable.getBalance())
                .subtract(creditCardPayable.getCreditLimit())
                .subtract(creditCardReceivable.getCreditLimit())
                .doubleValue();

        when(accountService.getUserAccounts(Mockito.any())).thenReturn(List.of(
                loanPayable,
                loanReceivable,
                creditCardPayable,
                creditCardReceivable
        ));
        var result = mainFacadeImpl.getMainPageData(UUID.randomUUID());

        assertEquals(expectedPayable, result.getAccountsPayable());
        assertEquals(expectedReceivable, result.getAccountsReceivable());
        assertEquals(expectedTotalBalance, result.getTotalBalance());
        assertEquals(0d, result.getAvailableBalance());
    }

    @Test
    void getMainPageData_whenNotCreditAccounts_thenAvailableBalanceAdd() {
        BigDecimal balance = BigDecimal.TEN;
        List<AccountDto> accounts = List.of(
                new CashAccountDto(balance),
                new BankAccountDto(balance),
                new DebitCardAccountDto(balance)
        );

        when(accountService.getUserAccounts(Mockito.any())).thenReturn(accounts);
        var result = mainFacadeImpl.getMainPageData(UUID.randomUUID());

        assertEquals(balance.multiply(new BigDecimal(accounts.size())).doubleValue(), result.getAvailableBalance());
        assertEquals(result.getAvailableBalance(), result.getTotalBalance());
        assertEquals(0d, result.getAccountsPayable());
        assertEquals(0d, result.getAccountsReceivable());
    }
}
