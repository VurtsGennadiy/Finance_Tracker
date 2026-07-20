package ru.vgd.tracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSender {
    private final MailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Async("emailSendExecutor")
    public void sendEmail(String to, String subject, String text) {
        log.debug("Отправка email to {}, subject {}", to, subject);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);

        mailSender.send(message);
        log.info("Отправлен email to {}, subject {}", to, subject);
    }
}

