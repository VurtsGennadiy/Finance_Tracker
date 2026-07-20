package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.token.EmailConfirmationToken;
import ru.vgd.tracker.dal.token.EmailConfirmationTokenRepository;
import ru.vgd.tracker.dal.user.User;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationService {
    private final EmailConfirmationTokenRepository repository;
    private final EmailSender emailSender;

    @Transactional
    public void issueEmailConfirmToken(User user) {
        log.debug("Создание токена подтверждения электронной почты для пользователя. userId: {}", user.getId());
        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setUserId(user.getId());
        repository.deleteByUserId(user.getId());
        repository.save(token);
        log.debug("Сохранён токен подтверждения электронной почты для пользователя. userId: {}", user.getId());
        sendConfirmationEmail(user, token);
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
