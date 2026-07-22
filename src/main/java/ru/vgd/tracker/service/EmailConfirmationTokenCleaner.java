package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.token.EmailConfirmationTokenRepository;

import java.time.Instant;

/**
 * Очищает базу данных от устаревших токенов
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConfirmationTokenCleaner {

    private final EmailConfirmationTokenRepository tokenRepository;

    @Async
    @Scheduled(initialDelayString = "PT1M", fixedRateString = "PT12H")
    @Transactional
    public void cleanExpiredTokens() {
        log.info("Очистка устаревших токенов подтверждения email");
        int deletedCount = tokenRepository.deleteAllByExpiredAtBefore(Instant.now());
        log.info("Освобождено {} записей в базе данных", deletedCount);
    }
}
