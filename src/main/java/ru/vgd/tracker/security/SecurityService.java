package ru.vgd.tracker.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    private final UserDetailsService detailsService;

    /**
     * Обновляет данные пользователя в SecurityContext.
     * Метод необходимо использовать после изменения данных пользователя.
     */
    public void refreshUser(String username) {
        log.trace("Обновление данных пользователя {} в SecurityContext", username);
        UserDetails freshUser = detailsService.loadUserByUsername(username);

        UsernamePasswordAuthenticationToken authToken =
                UsernamePasswordAuthenticationToken.authenticated(freshUser, null, freshUser.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authToken);
        log.trace("Данные пользователя {} в SecurityContext обновлены", username);
    }
}
