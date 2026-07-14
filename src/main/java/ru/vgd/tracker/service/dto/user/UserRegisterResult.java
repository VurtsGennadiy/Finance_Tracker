package ru.vgd.tracker.service.dto.user;

import ru.vgd.tracker.dal.user.User;

import java.util.List;
import java.util.Optional;

/**
 * Результат регистрации: пользователь при успехе, список ошибок при неудаче.
 */
public record UserRegisterResult(Optional<User> user, List<String> errors) {
    public boolean isSuccess() {
        return user.isPresent();
    }
}
