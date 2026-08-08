package ru.vgd.tracker.dal.repository;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import ru.vgd.tracker.config.TestcontainersConfig;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.dal.user.UserRepository;
import ru.vgd.tracker.dal.user.UserRole;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfig.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserRepositoryIT {

    private final UserRepository userRepository;
    private final TestEntityManager entityManager;

    private final String username = "test_user";
    private final String email = "test_user@email.com";
    private final Set<UserRole> roles = Set.of(UserRole.USER);

    @BeforeEach
    public void setup() {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setRoles(roles);
        user.setPassword("secret");

        userRepository.saveAndFlush(user);
        entityManager.flush();
    }

    @Test
    @DisplayName("Сохранение пользователя")
    void saveUser() {
        String username = "username";
        String password = "password";
        String email = "username@email.com";
        Set<UserRole> roles = Set.of(UserRole.USER);

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setEmail(email);
        user.setRoles(roles);

        User saved = userRepository.saveAndFlush(user);
        entityManager.clear();

        Optional<User> loadedOp = userRepository.findById(saved.getId());
        assertTrue(loadedOp.isPresent());
        User loaded = loadedOp.get();

        assertEquals(username, loaded.getUsername());
        assertEquals(password, loaded.getPassword());
        assertEquals(email, loaded.getEmail());
        assertEquals(roles, loaded.getRoles());
        assertFalse(loaded.isConfirmedEmail());
        assertEquals(0, loaded.getAccounts().size());
        assertNotNull(loaded.getCreatedAt());
    }

    @Test
    @DisplayName("Поиск пользователя по имени")
    void loadUserWithRolesByUsername() {
        Optional<User> loadedOp = userRepository.loadUserWithRolesByUsername(username);
        assertTrue(loadedOp.isPresent());
        User loaded = loadedOp.get();

        assertEquals(username, loaded.getUsername());
        assertEquals(email, loaded.getEmail());
        assertEquals(roles, loaded.getRoles());
    }

    @Test
    @DisplayName("Поиск пользователя по email")
    void loadUserWithRolesByEmail() {
        Optional<User> loadedOp = userRepository.loadUserWithRolesByEmail(email);
        assertTrue(loadedOp.isPresent());
        User loaded = loadedOp.get();

        assertEquals(username, loaded.getUsername());
        assertEquals(email, loaded.getEmail());
        assertEquals(roles, loaded.getRoles());
    }
}
