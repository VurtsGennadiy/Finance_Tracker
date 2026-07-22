package ru.vgd.tracker.dal.token;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface EmailConfirmationTokenRepository extends JpaRepository<EmailConfirmationToken, UUID> {


    int deleteAllByExpiredAtBefore(Instant expiredAt);
}
