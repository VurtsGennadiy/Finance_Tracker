package ru.vgd.tracker.dal.transaction;

import jakarta.persistence.*;
import lombok.*;
import ru.vgd.tracker.dal.account.entity.Account;
import ru.vgd.tracker.dal.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Описание транзакции
 */
@Entity
@Table(name = "transactions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Transaction {

    @Id
    @GeneratedValue
    @Column(name = "transaction_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "category", nullable = false)
    @Enumerated(EnumType.STRING)
    private Category category;

    @Column(name = "description")
    private String description;

    /**
     * Время создания записи в БД.
     */
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Дата совершения транзакции.
     */
    @Column(name = "transaction_date", nullable = false)
    @Builder.Default
    private LocalDate transactionDate = LocalDate.now();

    /**
     * Пользователь, создавший транзакцию.
     * Поскольку у одного счёта может быть несколько владельцев, то недостаточно только ссылки на счёт.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    /**
     * Связанная транзакция для переводов между счетами.
     * null для операций дохода и расхода.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_transaction_id")
    private Transaction relatedTransaction;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now().truncatedTo(ChronoUnit.MICROS);
    }

    /**
     * Возвращает timestamp в миллисекундах для клиентской локализации
     */
    public Long getCreatedAtTimestamp() {
        return createdAt != null ? createdAt.toEpochMilli() : null;
    }
}
