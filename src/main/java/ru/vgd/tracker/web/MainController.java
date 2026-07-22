package ru.vgd.tracker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.entity.AccountType;
import ru.vgd.tracker.dal.transaction.Category;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.service.dto.transaction.TransactionCreateRequest;
import ru.vgd.tracker.service.dto.transaction.TransactionDto;
import ru.vgd.tracker.service.dto.transaction.TransferCreateRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/")
public class MainController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public String indexPage(@AuthenticationPrincipal SecurityUser principal,
                            Model model) {

        List<AccountDto> accounts = accountService.getUserAccounts(principal.getUserId());
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (var account : accounts) {
            totalBalance = totalBalance.add(account.getBalance());
        }

        List<TransactionDto> recentTransactions = transactionService.getUserLastTransactions(principal.getUserId());

        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("transactions", recentTransactions);
        model.addAttribute("accounts", accounts);
        model.addAttribute("incomeCategories", Category.getIncomeCategories());
        model.addAttribute("expenseCategories", Category.getExpenseCategories());
        model.addAttribute("accountTypes", AccountType.values());

        UUID accountId = accounts.stream().map(AccountDto::getId).findFirst().orElse(null);
        model.addAttribute("transactionCreateRequest", new TransactionCreateRequest(accountId));
        model.addAttribute("transferCreateRequest", new TransferCreateRequest(accountId));

        if (!principal.getUser().isConfirmedEmail()) {
            model.addAttribute("emailConfirmedWarning", true);
        }
        return "index";
    }
}
