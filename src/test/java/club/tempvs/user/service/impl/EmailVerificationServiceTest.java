package club.tempvs.user.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.AssertionErrors.assertEquals;

import club.tempvs.user.component.EmailSender;
import club.tempvs.user.dao.EmailVerificationDao;
import club.tempvs.user.dao.UserDao;
import club.tempvs.user.domain.EmailVerification;
import club.tempvs.user.domain.User;
import club.tempvs.user.exception.UserAlreadyExistsException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class EmailVerificationServiceTest {

    @InjectMocks
    private EmailVerificationServiceImpl emailVerificationService;

    @Mock
    private EmailSender emailSender;
    @Mock
    private UserDao userDao;
    @Mock
    private EmailVerificationDao emailVerificationDao;

    @Mock
    private EmailVerification emailVerification;
    @Mock
    private User user;

    @Test
    public void testCreate() {
        String email = "test@email.com";

        when(emailVerificationDao.save(any(EmailVerification.class))).thenReturn(emailVerification);

        EmailVerification result = emailVerificationService.create(email);

        verify(emailSender).sendRegistrationVerification(eq(email), anyString());
        verify(emailVerificationDao).save(any(EmailVerification.class));
        verifyNoMoreInteractions(emailVerificationDao, emailSender);

        assertEquals("Email verification object is returned", emailVerification, result);
    }

    @Test
    public void testCreateForExisting() {
        String email = "test@email.com";
        Optional<User> userOptional = Optional.of(user);

        when(userDao.get(email)).thenReturn(userOptional);

        assertThrows(UserAlreadyExistsException.class, () -> {
            emailVerificationService.create(email);
        });
    }
}
