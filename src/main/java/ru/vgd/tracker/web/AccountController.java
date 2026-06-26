package ru.vgd.tracker.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vgd.tracker.dal.entity.Account;
import ru.vgd.tracker.dal.entity.CardType;
import ru.vgd.tracker.dal.entity.User;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.AccountService;
import ru.vgd.tracker.service.dto.CreateAccountRequest;
import java.util.List;

/**
 * MVC-контроллер для управления счетами через Thymeleaf
 */
@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

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
    public String createAccount(@Valid @ModelAttribute("request") CreateAccountRequest request,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("cardTypes", List.of(CardType.values()));
            return "accounts/create";
        }

        try {
            User user = accountService.getFirstUser();
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
