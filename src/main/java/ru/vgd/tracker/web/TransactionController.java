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
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.TransferCreateRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * MVC-контроллер для управления транзакциями через Thymeleaf
 */
@Controller
@RequestMapping("/accounts/{accountId}")
@RequiredArgsConstructor
public class TransactionController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    /**
     * Показывает форму перевода между счетами
     */
    @GetMapping("/transfer")
    public String showTransferForm(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            Model model) {

        TransferCreateRequest request = new TransferCreateRequest();
        request.setFromAccountId(accountId);
        model.addAttribute("request", request);

        populateTransferModel(accountId, principal.getUserId(), model);
        return "accounts/transfer";
    }

    /**
     * Обрабатывает форму перевода между счетами
     */
    @PostMapping("/transfer")
    public String createTransfer(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal SecurityUser principal,
            @Valid @ModelAttribute("request") TransferCreateRequest request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            populateTransferModel(accountId, principal.getUserId(), model);
            return "accounts/transfer";
        }

        try {
            User user = principal.getUser();
            transactionService.createTransferTransactions(request, user);
            redirectAttributes.addFlashAttribute("success",
                    "Перевод " + request.getAmount() + " ₽ успешно выполнен");
            return "redirect:/accounts/" + accountId;
        } catch (Exception e) {
            populateTransferModel(accountId, principal.getUserId(), model);
            model.addAttribute("error", "Ошибка при выполнении перевода: " + e.getMessage());
            return "accounts/transfer";
        }
    }

    /**
     * Заполняет модель данными счетов для формы перевода.
     * Вызывается только при необходимости повторного рендеринга формы (ошибки валидации, исключения).
     */
    private void populateTransferModel(UUID accountId, UUID userId, Model model) {
        List<Account> accountsList = accountService.getUserAccounts(userId);
        List<Account> otherAccounts = new ArrayList<>(accountsList.size() - 1);
        Account fromAccount = null;

        for (Account currentAccount : accountsList) {
            if (currentAccount.getId().equals(accountId)) {
                fromAccount = currentAccount;
            } else {
                otherAccounts.add(currentAccount);
            }
        }

        model.addAttribute("account", fromAccount);
        model.addAttribute("otherAccounts", otherAccounts);
    }
}
