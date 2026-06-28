package ru.vgd.tracker.exception;

/**
 * Исключение, которое выбрасывается при отсутствии прав доступа к ресурсу.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
