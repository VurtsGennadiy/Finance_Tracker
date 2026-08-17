package ru.vgd.tracker.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vgd.tracker.dal.account.entity.AccountType;
import ru.vgd.tracker.dal.transaction.Category;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.account.AccountCreateRequest;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.service.dto.transaction.TransactionCreateRequest;
import ru.vgd.tracker.service.dto.transaction.TransferCreateRequest;

import java.util.List;
import java.util.UUID;

/**
 * MVC-контроллер для управления счетами через Thymeleaf
 */
@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    /**
     * Отобразить страницу детальной информации по счёту
     */
    @GetMapping("/{accountId}")
    public String getAccountDetails(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            Model model) {

        List<AccountDto> accounts = accountService.getUserAccounts(principal.getUserId());
        AccountDto currentAccount = accounts.stream()
                .filter(account -> account.getId().equals(accountId))
                .findFirst()
                .orElseThrow(() -> new ItemNotFoundException(String.format("Счёт %s не найден", accountId)));

        var recentTransactions = transactionService.getAccountLastTransactions(accountId);

        model.addAttribute("account", currentAccount);
        model.addAttribute("accounts", accounts);

        model.addAttribute("transactions", recentTransactions);
        model.addAttribute("incomeCategories", Category.getIncomeCategories());
        model.addAttribute("expenseCategories", Category.getExpenseCategories());

        model.addAttribute("transactionCreateRequest", new TransactionCreateRequest(accountId));
        model.addAttribute("transferCreateRequest", new TransferCreateRequest(accountId));

        return "accounts/details";
    }

    /**
     * Отобразить форму создания нового счёта
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new AccountCreateRequest());
        model.addAttribute("accountTypes", AccountType.values());
        return "accounts/create";
    }

    /**
     * Обработка формы создания нового счёта
     */
    @PostMapping("/create")
    public String createAccount(
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @ModelAttribute("request") AccountCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("accountTypes", AccountType.values());
            return "accounts/create";
        }

        try {
            User user = principal.getUser();
            accountService.createAccount(request, user);
            redirectAttributes.addFlashAttribute("success", "Счёт «" + request.getName() + "» успешно создан!");
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("accountTypes", AccountType.values());
            model.addAttribute("error", "Ошибка при создании счёта: " + e.getMessage());
            return "accounts/create";
        }
    }

    /**
     * Удаление счёта
     */
    @PostMapping("/{accountId}/delete")
    public String deleteAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            RedirectAttributes redirectAttributes) {

        try {
            User user = principal.getUser();
            accountService.deleteAccount(accountId, user);
            redirectAttributes.addFlashAttribute("success", "Счёт успешно удалён");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении счёта: " + e.getMessage());
            return "redirect:/accounts/" + accountId;
        }
    }
}
