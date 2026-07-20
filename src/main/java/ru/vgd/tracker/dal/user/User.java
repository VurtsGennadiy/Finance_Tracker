package ru.vgd.tracker.dal.user;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ru.vgd.tracker.dal.account.entity.Account;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Пользователь-владелец счетов
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = "accounts")
public class User {

    @Id
    @GeneratedValue
    @Column(name = "user_id", nullable = false)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<UserRole> roles = EnumSet.of(UserRole.USER);

    @ManyToMany(mappedBy = "owners", fetch = FetchType.LAZY)
    private Set<Account> accounts = new HashSet<>();

    @Column(name = "confirmed_email", nullable = false)
    private boolean confirmedEmail = false;
}
