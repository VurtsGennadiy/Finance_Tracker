package ru.vgd.tracker.dal.account.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import ru.vgd.tracker.dal.user.User;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Базовый класс для денежного счёта
 */
@Entity
@Table(name = "accounts_base")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "account_type", discriminatorType = DiscriminatorType.STRING)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "owners")
public abstract class Account {

    @Id
    @GeneratedValue
    @Column(name = "account_id", nullable = false)
    private UUID id;

    @Column(name = "name", nullable = false)
    @EqualsAndHashCode.Include
    private String name;

    @Column(name = "balance", nullable = false)
    @EqualsAndHashCode.Include
    private BigDecimal balance = BigDecimal.ZERO;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "account_owners",
            joinColumns = @JoinColumn(name = "account_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @Builder.Default
    private Set<User> owners = new HashSet<>();

    @Column(name = "account_type", insertable = false, updatable = false)
    @Enumerated(EnumType.STRING)
    private AccountType accountType;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp(source = SourceType.DB)
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp(source = SourceType.DB)
    private Instant updatedAt;

    /**
     * Определение, является ли счёт кредитным.
     * @return true если счёт кредитный, false если счёт не кредитный
     */
    public abstract boolean isCreditAccount();
}
