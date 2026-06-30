package ru.vgd.tracker.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.entity.CardType;
import ru.vgd.tracker.dal.transaction.Category;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.CreateAccountRequest;
import ru.vgd.tracker.service.dto.TransactionCreateRequest;

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
     * Отображает список всех счетов текущего пользователя
     */
    @GetMapping
    public String listAccounts(
            @AuthenticationPrincipal SecurityUser principal,
            Model model) {

        List<Account> accounts = accountService.getUserAccounts(principal.getUserId());
        model.addAttribute("accounts", accounts);
        return "accounts/list";
    }

    /**
     * Отображает детальную информацию по счёту
     */
    @GetMapping("/{accountId}")
    public String viewAccount(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            Model model) {

        Account account = accountService.getAccountById(accountId, principal.getUserId());
        var transactions = transactionService.getAccountTransactions(accountId);
        
        model.addAttribute("account", account);
        model.addAttribute("transactions", transactions);
        return "accounts/details";
    }

    /**
     * Показывает форму пополнения счёта
     */
    @GetMapping("/{accountId}/income")
    public String showDepositForm(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            Model model) {

        Account account = accountService.getAccountById(accountId, principal.getUserId());
        
        model.addAttribute("account", account);
        model.addAttribute("request", new TransactionCreateRequest(accountId));
        model.addAttribute("incomeCategories", Category.getIncomeCategories());
        return "accounts/income";
    }

    /**
     * Обрабатывает форму пополнения счёта
     */
    @PostMapping("/{accountId}/income")
    public String createDeposit(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @ModelAttribute("request") TransactionCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("account", accountService.getAccountById(accountId, principal.getUserId()));
            model.addAttribute("incomeCategories", Category.getIncomeCategories());
            return "accounts/income";
        }

        try {
            User user = principal.getUser();
            transactionService.createIncomeTransaction(request, user);
            redirectAttributes.addFlashAttribute("success", "Счёт успешно пополнен на " + 
                    request.getAmount() + " ₽");
            return "redirect:/accounts/" + accountId;
        } catch (Exception e) {
            model.addAttribute("account", accountService.getAccountById(accountId, principal.getUserId()));
            model.addAttribute("incomeCategories", Category.getIncomeCategories());
            model.addAttribute("error", "Ошибка при пополнении: " + e.getMessage());
            return "accounts/income";
        }
    }

    /**
     * Показывает форму расходной транзакции
     */
    @GetMapping("/{accountId}/expense")
    public String showExpenseForm(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            Model model) {

        Account account = accountService.getAccountById(accountId, principal.getUserId());

        model.addAttribute("account", account);
        model.addAttribute("request", new TransactionCreateRequest(accountId));
        model.addAttribute("expenseCategories", Category.getExpenseCategories());
        return "accounts/expense";
    }

    /**
     * Обрабатывает форму расходной транзакции
     */
    @PostMapping("/{accountId}/expense")
    public String createExpense(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @ModelAttribute("request") TransactionCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("account", accountService.getAccountById(accountId, principal.getUserId()));
            model.addAttribute("expenseCategories", Category.getExpenseCategories());
            return "accounts/expense";
        }

        try {
            User user = principal.getUser();
            transactionService.createExpenseTransaction(request, user);
            redirectAttributes.addFlashAttribute("success", "Расход " +
                    request.getAmount() + " ₽ успешно записан");
            return "redirect:/accounts/" + accountId;
        } catch (Exception e) {
            model.addAttribute("account", accountService.getAccountById(accountId, principal.getUserId()));
            model.addAttribute("expenseCategories", Category.getExpenseCategories());
            model.addAttribute("error", "Ошибка при создании расхода: " + e.getMessage());
            return "accounts/expense";
        }
    }

    /**
     * Показывает форму создания нового счёта
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new CreateAccountRequest());
        model.addAttribute("cardTypes", CardType.values());
        return "accounts/create";
    }

    /**
     * Обрабатывает форму создания нового счёта
     */
    @PostMapping("/create")
    public String createAccount(
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @ModelAttribute("request") CreateAccountRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("cardTypes", List.of(CardType.values()));
            return "accounts/create";
        }

        try {
            User user = principal.getUser();
            accountService.createAccount(request, user);
            redirectAttributes.addFlashAttribute("success", "Счёт «" + request.getName() + "» успешно создан!");
            return "redirect:/accounts";
        } catch (Exception e) {
            model.addAttribute("cardTypes", List.of(CardType.values()));
            model.addAttribute("error", "Ошибка при создании счёта: " + e.getMessage());
            return "accounts/create";
        }
    }
}
