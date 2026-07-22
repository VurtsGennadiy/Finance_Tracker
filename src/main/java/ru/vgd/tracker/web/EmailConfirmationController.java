package ru.vgd.tracker.web;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.EmailConfirmationService;

import java.util.UUID;

/**
 * Контроллер подтверждения email
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("email-confirm")
public class EmailConfirmationController {

    private final EmailConfirmationService service;

    /**
     * Подтверждение email
     */
    @GetMapping
    public String confirmEmail(@RequestParam("token") UUID token,
                               RedirectAttributes redirectAttributes) {
        try {
            service.confirmEmail(token);
            redirectAttributes.addFlashAttribute("success", "Email подтверждён");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Email не подтверждён. Причина: " + e.getMessage());
        }
        return "redirect:/";
    }

    /**
     * Повторная отправка ссылки подтверждения
     */
    @PostMapping("/resend")
    public String resendLink(@AuthenticationPrincipal SecurityUser principal,
                             RedirectAttributes redirectAttributes) {

        User user = principal.getUser();
        service.issueEmailConfirmToken(user);
        redirectAttributes.addFlashAttribute("info", "Ссылка для подтверждения отправлена на " + user.getEmail());
        return "redirect:/";
    }
}
