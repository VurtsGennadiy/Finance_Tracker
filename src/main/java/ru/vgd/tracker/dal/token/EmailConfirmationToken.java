package ru.vgd.tracker.dal.token;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Токен для подтверждения электронной почты
 */
@Entity
@Table(name = "email_confirmation_tokens")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class EmailConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    @Column(name = "token", nullable = false)
    private UUID token;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;

    @PrePersist
    public void setupExpiredAt() {
        if (expiredAt == null) {
            expiredAt = Instant.now().plus(1, ChronoUnit.DAYS);
        }
    }
}
