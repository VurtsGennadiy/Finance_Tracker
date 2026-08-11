package ru.vgd.tracker.service;

import ru.vgd.tracker.dal.user.User;

import java.util.UUID;

public interface EmailConfirmationService {
    void issueEmailConfirmToken(User user);

    void confirmEmail(UUID token);
}
