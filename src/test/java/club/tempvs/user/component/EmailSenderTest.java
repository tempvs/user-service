package club.tempvs.user.component;

import club.tempvs.user.dto.EmailDto;
import club.tempvs.user.http.EmailHttpClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.Locale;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailSenderTest {

    private static final Locale locale = Locale.ENGLISH;

    @InjectMocks
    private EmailSender emailSender;

    @Mock
    private MessageSource messageSource;
    @Mock
    private EmailBuilder emailBuilder;
    @Mock
    private EmailHttpClient emailHttpClient;

    @BeforeAll
    public static void setUp() {
        LocaleContextHolder.setLocale(locale);
    }

    @Test
    public void testSendRegistrationVerification() {
        //given
        String email = "test@email.com";
        String subject = "user.registration.email.subject";
        String bodyMessage = "user.registration.email.body.message";
        String buttonText = "user.registration.email.body.button";
        String baseUrl = "http://localhost:8080";
        emailSender.setBaseUrl(baseUrl);
        String verificationId = "verificationId";
        String link = baseUrl + "/user/registration/" + verificationId;
        String body = "body";
        EmailDto payload = new EmailDto(email, subject, body);

        when(messageSource.getMessage(subject, null, subject, locale)).thenReturn(subject);
        when(messageSource.getMessage(bodyMessage, null, bodyMessage, locale)).thenReturn(bodyMessage);
        when(messageSource.getMessage(buttonText, null, buttonText, locale)).thenReturn(buttonText);
        when(emailBuilder.buildRegistrationVerification(bodyMessage, link, buttonText)).thenReturn(body);

        //when
        emailSender.sendRegistrationVerification(email, verificationId);

        //then
        verify(emailBuilder).buildRegistrationVerification(bodyMessage, link, buttonText);
        verify(emailHttpClient).post(payload);
        verifyNoMoreInteractions(emailBuilder, emailHttpClient);
    }
}
