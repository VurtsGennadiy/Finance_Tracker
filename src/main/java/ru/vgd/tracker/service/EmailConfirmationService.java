package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.token.EmailConfirmationToken;
import ru.vgd.tracker.dal.token.EmailConfirmationTokenRepository;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.dal.user.UserRepository;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.security.SecurityService;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationService {
    private final EmailSender emailSender;
    private final EmailConfirmationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final SecurityService securityService;

    @Transactional
    public void issueEmailConfirmToken(User user) {
        log.debug("Создание токена подтверждения электронной почты для пользователя. userId: {}", user.getId());
        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setUserId(user.getId());
        tokenRepository.save(token);
        log.debug("Сохранён токен подтверждения электронной почты для пользователя. userId: {}", user.getId());
        sendConfirmationEmail(user, token);
    }

    @Transactional
    public void confirmEmail(UUID token) {
        log.debug("Запрос подтверждения электронной почты. token: {}", token);

        var confirmationToken = tokenRepository.findById(token)
                .orElseThrow(() -> new ItemNotFoundException("Токен " + token + " не найден"));

        User user = userRepository.findById(confirmationToken.getUserId()).orElseThrow();

        if (confirmationToken.getExpiredAt().compareTo(Instant.now()) < 0) {
            throw new IllegalStateException("Срок жизни токена истек");
        }

        user.setConfirmedEmail(true);
        userRepository.saveAndFlush(user);
        securityService.refreshUser(user.getUsername());
        log.info("Электронная почта пользователя подтверждена. userId: {}", user.getId());
        tokenRepository.deleteById(token);
        log.debug("Токен подтверждения электронной почты удалён. token: {}", token);
    }

    private void sendConfirmationEmail(User user, EmailConfirmationToken token) {
        long lifetimeHours = Duration.between(token.getCreatedAt(), token.getExpiredAt()).toHours();
        String userMessage = """
                %s, cпасибо за регистрацию в приложении walletcontrol!
                
                Для подтверждения электронной почты перейдите по ссылке: %s.
                Ссылка действительна в течение %d часов.
                """.formatted(user.getUsername(), "https://walletcontrol.ru/email-confirm?token=" + token.getToken(), lifetimeHours);

        emailSender.sendEmail(user.getEmail(), "WalletControl. Подтверждение электронной почты", userMessage);
    }
}
