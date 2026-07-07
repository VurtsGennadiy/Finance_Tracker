package ru.vgd.tracker.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.account.entity.AccountType;
import ru.vgd.tracker.dal.transaction.Category;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.TransactionService;
import ru.vgd.tracker.service.dto.TransactionCreateRequest;
import ru.vgd.tracker.service.dto.TransactionDto;
import ru.vgd.tracker.service.dto.TransferCreateRequest;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/home")
public class HomeController {

    private final AccountService accountService;
    private final TransactionService transactionService;

    @GetMapping
    public String homePage(@AuthenticationPrincipal SecurityUser securityUser,
                           Model model) {

        List<Account> accounts = accountService.getUserAccounts(securityUser.getUserId());
        BigDecimal totalBalance = BigDecimal.ZERO;
        for (Account account : accounts) {
            totalBalance = totalBalance.add(account.getBalance());
        }

        List<TransactionDto> recentOperations = transactionService.getUserLastTransactions(securityUser.getUserId());

        model.addAttribute("totalBalance", totalBalance);
        model.addAttribute("recentOperations", recentOperations);
        model.addAttribute("accounts", accounts);
        model.addAttribute("incomeCategories", Category.getIncomeCategories());
        model.addAttribute("expenseCategories", Category.getExpenseCategories());
        model.addAttribute("accountTypes", AccountType.values());
        return "home";
    }

    @PostMapping("/income")
    public String createIncome(@AuthenticationPrincipal SecurityUser securityUser,
                               @Valid @ModelAttribute TransactionCreateRequest request,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            populateBindingErrors(bindingResult, redirectAttributes);
            return "redirect:/home";
        }

        try {
            User user = securityUser.getUser();
            transactionService.createIncomeTransaction(request, user);
            redirectAttributes.addFlashAttribute("success", "Доход успешно добавлен");
            redirectAttributes.addFlashAttribute("showUndoButton", true);
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении дохода: " + e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/expense")
    public String createExpense(@AuthenticationPrincipal SecurityUser securityUser,
                                @Valid @ModelAttribute TransactionCreateRequest request,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            populateBindingErrors(bindingResult, redirectAttributes);
            return "redirect:/home";
        }

        try {
            User user = securityUser.getUser();
            transactionService.createExpenseTransaction(request, user);
            redirectAttributes.addFlashAttribute("success", "Расход успешно добавлен");
            redirectAttributes.addFlashAttribute("showUndoButton", true);
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении расхода: " + e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/transfer")
    public String createTransfer(@AuthenticationPrincipal SecurityUser securityUser,
                                 @Valid @ModelAttribute TransferCreateRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            populateBindingErrors(bindingResult, redirectAttributes);
            return "redirect:/home";
        }

        try {
            User user = securityUser.getUser();
            transactionService.createTransferTransactions(request, user);
            redirectAttributes.addFlashAttribute("success", "Перевод успешно добавлен");
            redirectAttributes.addFlashAttribute("showUndoButton", true);
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при добавлении перевода: " + e.getMessage());
            return "redirect:/home";
        }
    }

    @PostMapping("/recent/undo")
    public String undoRecentTransaction(@AuthenticationPrincipal SecurityUser securityUser,
                                        RedirectAttributes redirectAttributes) {
        try {
            transactionService.cancelLastTransaction(securityUser.getUserId());
            redirectAttributes.addFlashAttribute("success", "Операция отменена");
            return "redirect:/home";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при отмене операции: " + e.getMessage());
            return "redirect:/home";
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
}
