package ru.vgd.tracker.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import ru.vgd.tracker.exception.AccessDeniedException;
import ru.vgd.tracker.exception.ItemNotFoundException;
import ru.vgd.tracker.security.SecurityUser;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Добавляет общие атрибуты в модель для всех контроллеров
 * и обрабатывает исключения глобально.
 */
@ControllerAdvice
@Slf4j
public class GlobalControllerAdvice {

    @ModelAttribute("username")
    public String username(@AuthenticationPrincipal SecurityUser principal) {
        return principal != null ? principal.getUsername() : null;
    }

    /**
     * Обработка исключения "Элемент не найден"
     */
    @ExceptionHandler({ItemNotFoundException.class, NoResourceFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleItemNotFoundException(ItemNotFoundException ex, HttpServletRequest request) {
        log.warn("Item not found: {}", ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/not-found");
        mav.setStatus(HttpStatus.NOT_FOUND);
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("errorCode", "404");
        mav.addObject("requestUrl", request.getRequestURI());
        return mav;
    }

    /**
     * Обработка исключения "Доступ запрещён"
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handleAccessDeniedException(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/access-denied");
        mav.setStatus(HttpStatus.FORBIDDEN);
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("errorCode", "403");
        mav.addObject("requestUrl", request.getRequestURI());
        return mav;
    }

    /**
     * Обработка IllegalArgumentException (ошибки валидации входных данных)
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ModelAndView handleIllegalArgumentException(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        
        ModelAndView mav = new ModelAndView("error/bad-request");
        mav.setStatus(HttpStatus.BAD_REQUEST);
        mav.addObject("errorMessage", ex.getMessage());
        mav.addObject("errorCode", "400");
        mav.addObject("requestUrl", request.getRequestURI());
        return mav;
    }

    /**
     * Общий обработчик для всех необработанных исключений
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        
        ModelAndView mav = new ModelAndView("error/internal-error");
        mav.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        mav.addObject("errorMessage", "Произошла непредвиденная ошибка");
        mav.addObject("errorCode", "500");
        mav.addObject("requestUrl", request.getRequestURI());
        
        // В development режиме можно показать детали ошибки
        // В production лучше не показывать
        mav.addObject("errorDetails", ex.getMessage());
        
        return mav;
    }
}
