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
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.AccountCreateRequest;

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
     * Показывает форму создания нового счёта
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("request", new AccountCreateRequest());
        model.addAttribute("cardTypes", CardType.values());
        return "accounts/create";
    }

    /**
     * Обрабатывает форму создания нового счёта
     */
    @PostMapping("/create")
    public String createAccount(
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @ModelAttribute("request") AccountCreateRequest request,
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

    /**
     * Удаляет счёт текущего пользователя
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
            return "redirect:/accounts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении счёта: " + e.getMessage());
            return "redirect:/accounts/" + accountId;
        }
    }
}
