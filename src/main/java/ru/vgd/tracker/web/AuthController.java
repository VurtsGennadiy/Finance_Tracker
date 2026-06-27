package ru.vgd.tracker.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.security.SecurityUser;
import ru.vgd.tracker.service.UserService;
import ru.vgd.tracker.service.dto.UserRegisterRequest;
import ru.vgd.tracker.service.dto.UserRegisterResult;

/**
 * Контроллер аутентификации и регистрации.
 * POST /login обрабатывается Spring Security автоматически.
 */
@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("registerRequest", new UserRegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerRequest") UserRegisterRequest request,
                           BindingResult bindingResult,
                           Model model,
                           HttpServletRequest httpRequest) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        UserRegisterResult result = userService.register(request);
        if (!result.isSuccess()) {
            model.addAttribute("errors", result.errors());
            return "auth/register";
        }

        // Автоматический вход после регистрации
        User user = result.user().orElseThrow();
        SecurityUser securityUser = new SecurityUser(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                securityUser, null, securityUser.getAuthorities()
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        httpRequest.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context
        );

        return "redirect:/accounts";
    }
}
