package ru.vgd.tracker.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vgd.tracker.dal.transaction.TransactionType;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.account.AccountDto;
import ru.vgd.tracker.service.dto.transaction.TransactionCreateRequest;
import ru.vgd.tracker.service.dto.transaction.TransactionDto;
import ru.vgd.tracker.service.dto.transaction.TransactionFilter;
import ru.vgd.tracker.service.dto.transaction.TransferCreateRequest;

import java.math.BigDecimal;
import java.util.List;

/**
 * MVC-контроллер для управления транзакциями через Thymeleaf
 */
@Controller
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    /**
     * Показать транзакции по счетам пользователя
     */
    @GetMapping
    public String getTransactions(
            @ModelAttribute TransactionFilter filter,
            @PageableDefault(size = 30, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable,
            Model model,
            @AuthenticationPrincipal SecurityUser principal
    ) {

        List<AccountDto> userAccounts = accountService.getUserAccounts(principal.getUserId());
        if (filter.getAccounts().isEmpty()) filter.setAccounts(userAccounts.stream().map(AccountDto::getId).toList());

        Page<TransactionDto> transactionPage = transactionService.getTransactions(filter, pageable);
        BigDecimal totalIncomes = BigDecimal.ZERO;
        BigDecimal totalExpenses = BigDecimal.ZERO;

        for (TransactionDto transaction : transactionPage.getContent()) {
            if (TransactionType.INCOME == transaction.getType()) {
                totalIncomes = totalIncomes.add(transaction.getAmount());
            } else if (TransactionType.EXPENSE == transaction.getType()) {
                totalExpenses = totalExpenses.add(transaction.getAmount());
            }
        }

        model.addAttribute("filter", filter);
        model.addAttribute("transactions", transactionPage.getContent());
        model.addAttribute("accounts", userAccounts);
        model.addAttribute("currentPage", transactionPage.getNumber());
        model.addAttribute("totalPages", transactionPage.getTotalPages());
        model.addAttribute("totalIncomes", totalIncomes);
        model.addAttribute("totalExpenses", totalExpenses);

        return "transactions/list";
    }

    /**
     * Создание доходной транзакции из модального окна страницы index
     */
    @PostMapping("/income")
    public String createIncome(@AuthenticationPrincipal SecurityUser securityUser,
                               @Valid @ModelAttribute("transactionCreateRequest") TransactionCreateRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpServletRequest httpRequest) {

        String redirectUri = "redirect:" + extractRedirectUri(httpRequest);

        if (bindingResult.hasErrors()) {
            populateBindingErrors(bindingResult, redirectAttributes);
            return redirectUri;
        }

        try {
            User user = securityUser.getUser();
            transactionService.createIncomeTransaction(request, user);
            redirectAttributes.addFlashAttribute("success", "Доход успешно добавлен");
            redirectAttributes.addFlashAttribute("showUndoButton", true);
            return redirectUri;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении дохода: " + e.getMessage());
            return redirectUri;
        }
    }

    /**
     * Создание расходной транзакции из модального окна страницы index
     */
    @PostMapping("/expense")
    public String createExpense(@AuthenticationPrincipal SecurityUser securityUser,
                                @Valid @ModelAttribute("transactionCreateRequest") TransactionCreateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes,
                                HttpServletRequest httpRequest) {

        String redirectUri = "redirect:" + extractRedirectUri(httpRequest);

        if (bindingResult.hasErrors()) {
            populateBindingErrors(bindingResult, redirectAttributes);
            return redirectUri;
        }

        try {
            User user = securityUser.getUser();
            transactionService.createExpenseTransaction(request, user);
            redirectAttributes.addFlashAttribute("success", "Расход успешно добавлен");
            redirectAttributes.addFlashAttribute("showUndoButton", true);
            return redirectUri;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении расхода: " + e.getMessage());
            return redirectUri;
        }
    }

    /**
     * Создание перевода между счетами из модального окна страницы index
     */
    @PostMapping("/transfer")
    public String createTransfer(@AuthenticationPrincipal SecurityUser securityUser,
                                 @Valid @ModelAttribute("transferCreateRequest") TransferCreateRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes,
                                 HttpServletRequest httpRequest) {

        String redirectUri = "redirect:" + extractRedirectUri(httpRequest);

        if (bindingResult.hasErrors()) {
            populateBindingErrors(bindingResult, redirectAttributes);
            return redirectUri;
        }

        try {
            User user = securityUser.getUser();
            transactionService.createTransferTransactions(request, user);
            redirectAttributes.addFlashAttribute("success", "Перевод успешно добавлен");
            redirectAttributes.addFlashAttribute("showUndoButton", true);
            return redirectUri;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении перевода: " + e.getMessage());
            return redirectUri;
        }
    }

    /**
     * Отмена последней транзакции введённой в модальном окне страницы index
     */
    @PostMapping("/recent/undo")
    public String undoRecentTransaction(@AuthenticationPrincipal SecurityUser securityUser,
                                        RedirectAttributes redirectAttributes) {
        try {
            transactionService.cancelLastTransaction(securityUser.getUserId());
            redirectAttributes.addFlashAttribute("success", "Операция отменена");
            return "redirect:/";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отмене операции: " + e.getMessage());
            return "redirect:/";
        }
    }

    /**
     * Наполняет redirectAttributes ошибками валидации
     */
    private void populateBindingErrors(BindingResult bindingResult, RedirectAttributes redirectAttributes) {
        List<String> errorMessages = bindingResult.getAllErrors().stream()
                .map(error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Ошибка валидации")
                .toList();

        redirectAttributes.addFlashAttribute("validationErrors", errorMessages);
    }

    /**
     * Извлекает URI страницы, с которой была отправлена форма
     */
    private String extractRedirectUri(HttpServletRequest httpRequest) {
        String redirectUri = httpRequest.getHeader("Referer");
        if (redirectUri == null) {
            redirectUri = "/";
        }

        return redirectUri;
    }
}
