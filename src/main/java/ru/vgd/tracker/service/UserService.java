package ru.vgd.tracker.service;

import ru.vgd.tracker.service.dto.user.UserRegisterRequest;
import ru.vgd.tracker.service.dto.user.UserRegisterResult;

public interface UserService {
    UserRegisterResult register(UserRegisterRequest request);
}
