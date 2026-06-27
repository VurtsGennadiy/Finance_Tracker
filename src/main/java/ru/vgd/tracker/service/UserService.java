package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.vgd.tracker.dal.user.User;
import ru.vgd.tracker.dal.user.UserRepository;
import ru.vgd.tracker.service.dto.UserRegisterRequest;
import ru.vgd.tracker.service.dto.UserRegisterResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    /**
     * Зарегистрировать нового пользователя.
     */
    @Transactional
    public UserRegisterResult register(UserRegisterRequest request) {
        log.debug("Запрос регистрации нового пользователя. username: {}, email: {}",
                request.getUsername(), request.getEmail());

        List<String> errors = new ArrayList<>();
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            errors.add("Пароли не совпадают");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            errors.add("Пользователь с таким именем уже существует");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            errors.add("Пользователь с таким email уже существует");
        }

        if (!errors.isEmpty()) {
            log.warn("Ошибка при регистрации пользователя. {}", errors);
            return new UserRegisterResult(Optional.empty(), errors);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);
        log.info("Пользователь успешно зарегистрирован. userId: {}", user.getId());
        return new UserRegisterResult(Optional.of(user), List.of());
    }
}
