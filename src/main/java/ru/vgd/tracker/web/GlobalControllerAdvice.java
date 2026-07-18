package ru.vgd.tracker.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.vgd.tracker.security.SecurityUser;

/**
 * Добавляет общие атрибуты в модель для всех контроллеров
 */
@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    @ModelAttribute("username")
    public String username(@AuthenticationPrincipal SecurityUser principal) {
        return principal != null ? principal.getUsername() : null;
    }
}
