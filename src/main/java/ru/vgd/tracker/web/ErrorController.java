package ru.vgd.tracker.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.webmvc.error.ErrorAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@Controller
@RequestMapping("/error")
@RequiredArgsConstructor
@Slf4j
public class ErrorController implements org.springframework.boot.webmvc.error.ErrorController {

    private final ErrorAttributes errorAttributes;

    @GetMapping
    public Object handleError(WebRequest request, Model model) {

        Throwable throwable = errorAttributes.getError(request);

        ErrorAttributeOptions options = ErrorAttributeOptions.of(
                ErrorAttributeOptions.Include.STATUS,
                ErrorAttributeOptions.Include.PATH,
                ErrorAttributeOptions.Include.MESSAGE,
                ErrorAttributeOptions.Include.STACK_TRACE
        );

        Map<String, Object> errorMap = errorAttributes.getErrorAttributes(request, options);
        Integer status = (Integer) errorMap.getOrDefault("status", 500);
        String message = (String) errorMap.getOrDefault("message", "Ошибка сервера");
        String path = (String) errorMap.getOrDefault("path", "неизвестно");
        String stackTrace = (String) errorMap.get("trace");

        if (throwable != null) {
            log.error("Ошибка обработки запроса. Статус {}, путь {}: {}", status, path, stackTrace);
        }

        // Закладываем возможность отдавать пользователю переопределенное сообщение об ошибке.
        // Проверяем, есть ли такое сообщение в атрибутах запроса.
        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletWebRequest.getNativeRequest();

            String userMessage = (String) httpServletRequest.getAttribute("USER_MESSAGE");
            if (userMessage != null && !userMessage.isBlank()) {
                message = userMessage;
            }
        }

        model.addAttribute("errorMessage", message);
        model.addAttribute("errorDetails", stackTrace);
        model.addAttribute("status", status);

        return resolveViewName(status);
    }

    private String resolveViewName(Integer status) {
        if (status == 403) return "error/403";
        if (status == 404) return "error/404";
        if (status >= 400 && status < 500) return "error/4xx";
        return "error/500";
    }
}
