package ru.vgd.tracker.exception;

/**
 * Ошибка удаления счёта
 */
public class AccountDeleteException extends RuntimeException {
    public AccountDeleteException(String message) {
        super(message);
    }
}
