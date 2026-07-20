package ru.vgd.tracker.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.vgd.tracker.dal.token.EmailConfirmationToken;
import ru.vgd.tracker.dal.token.EmailConfirmationTokenRepository;

import java.time.Duration;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@ExtendWith(MockitoExtension.class)
public class EmailConfirmationServiceTest {

    @Mock
    private EmailConfirmationTokenRepository repository;

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private EmailConfirmationService service;

/*    @Test
    public void issueEmailConfirmToken() {
        UUID tokenUuid = UUID.randomUUID();
        String mailTo = "test@example.com";


        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setToken(tokenUuid);
    }*/

/*    @Test
    public void tokenDefaultExpireTimeIs24H() {
        long expectedExpireTimeHour = 24;

        UUID tokenUuid = UUID.randomUUID();
        EmailConfirmationToken token = new EmailConfirmationToken();
        token.setToken(tokenUuid);
        token.setUserId(UUID.randomUUID());

        repository.save(token);
        long actualExpireTimeHour = Duration.between(token.getCreatedAt(), token.getExpiredAt()).toHours();

        assertEquals(expectedExpireTimeHour, actualExpireTimeHour);
    }*/
}
