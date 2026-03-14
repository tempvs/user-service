package club.tempvs.user.service.impl;

import club.tempvs.user.dao.EmailVerificationDao;
import club.tempvs.user.dao.UserDao;
import club.tempvs.user.domain.EmailVerification;
import club.tempvs.user.domain.User;
import club.tempvs.user.exception.UnauthorizedException;
import club.tempvs.user.exception.UserAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailVerificationDao emailVerificationDao;
    @Mock
    private UserDao userDao;

    @Mock
    private User user;
    @Mock
    private EmailVerification emailVerification;

    @Test
    public void testRegister() {
        String verificationId = "verification id";
        String email = "test@email.com";
        String password = "password";
        String encodedPassword = "encoded password";
        User preparedUser = new User(email, encodedPassword);
        Optional<EmailVerification> emailVerificationOptional = Optional.of(emailVerification);

        when(emailVerificationDao.get(verificationId)).thenReturn(emailVerificationOptional);
        when(emailVerification.getEmail()).thenReturn(email);
        when(userDao.get(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userDao.save(preparedUser)).thenReturn(user);

        User result = userService.register(verificationId, password);

        verify(emailVerificationDao).get(verificationId);
        verify(emailVerificationDao).delete(emailVerification);
        verify(userDao).get(email);
        verify(passwordEncoder).encode(password);
        verify(userDao).save(preparedUser);
        verifyNoMoreInteractions(passwordEncoder, userDao);

        assertEquals("User object is returned", user, result);
    }

    @Test
    public void testRegisterForDuplicate() {
        String verificationId = "verification id";
        String email = "test@email.com";
        String password = "password";
        Optional<EmailVerification> emailVerificationOptional = Optional.of(emailVerification);

        when(emailVerificationDao.get(verificationId)).thenReturn(emailVerificationOptional);
        when(emailVerification.getEmail()).thenReturn(email);
        when(userDao.get(email)).thenReturn(Optional.of(user));

        assertThrows(UserAlreadyExistsException.class, () -> {
            userService.register(verificationId, password);
        });
    }

    @Test
    public void testLogin() {
        String email = "email@test.com";
        String password = "password";
        String encodedPassword = "encodedPassword";
        Optional<User> userOptional = Optional.of(user);

        when(userDao.get(email)).thenReturn(userOptional);
        when(user.getPassword()).thenReturn(encodedPassword);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);

        User result = userService.login(email, password);

        verify(userDao).get(email);
        verify(passwordEncoder).matches(password, encodedPassword);
        verifyNoMoreInteractions(userDao, passwordEncoder);

        assertEquals("User object is returned", user, result);
    }

    @Test
    public void testLoginForWrongCredentials() {
        String email = "email@test.com";
        String password = "password";
        String encodedPassword = "encodedPassword";
        Optional<User> userOptional = Optional.of(user);

        when(userDao.get(email)).thenReturn(userOptional);
        when(user.getPassword()).thenReturn(encodedPassword);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> {
            userService.login(email, password);
        });
    }

    @Test
    public void testLoginForMissingUser() {
        String email = "email@test.com";
        String password = "password";
        Optional<User> userOptional = Optional.empty();

        when(userDao.get(email)).thenReturn(userOptional);

        assertThrows(NoSuchElementException.class, () -> {
            userService.login(email, password);
        });
    }

    @Test
    public void testCreateExternalUserForExistingExternalId() {
        String externalId = "google-sub-1";
        String email = "test@email.com";

        when(userDao.getByExternalId(externalId)).thenReturn(Optional.of(user));

        User result = userService.createExternalUser(externalId, email);

        verify(userDao).getByExternalId(externalId);
        verify(userDao, never()).get(email);
        verify(userDao, never()).save(any(User.class));
        assertEquals("User object is returned", user, result);
    }

    @Test
    public void testCreateExternalUserForExistingEmail() {
        String externalId = "google-sub-2";
        String email = "test@email.com";

        when(userDao.getByExternalId(externalId)).thenReturn(Optional.empty());
        when(userDao.get(email)).thenReturn(Optional.of(user));
        when(userDao.save(user)).thenReturn(user);

        User result = userService.createExternalUser(externalId, email);

        verify(userDao).getByExternalId(externalId);
        verify(userDao).get(email);
        verify(user).setExternalId(externalId);
        verify(userDao).save(user);
        assertEquals("User object is returned", user, result);
    }

    @Test
    public void testCreateExternalUserForNewEmail() {
        String externalId = "google-sub-3";
        String email = "new@email.com";

        when(userDao.getByExternalId(externalId)).thenReturn(Optional.empty());
        when(userDao.get(email)).thenReturn(Optional.empty());
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createExternalUser(externalId, email);

        verify(userDao).getByExternalId(externalId);
        verify(userDao).get(email);
        verify(userDao).save(any(User.class));
        assertEquals("External id is set", externalId, result.getExternalId());
        assertEquals("Email is set", email, result.getEmail());
    }

    @Test
    public void testCreateExternalUserForAlreadyLinkedEmail() {
        String externalId = "google-sub-4";
        String existingExternalId = "google-sub-existing";
        String email = "existing@email.com";

        when(userDao.getByExternalId(externalId)).thenReturn(Optional.empty());
        when(userDao.get(email)).thenReturn(Optional.of(user));
        when(user.getExternalId()).thenReturn(existingExternalId);

        User result = userService.createExternalUser(externalId, email);

        verify(userDao).getByExternalId(externalId);
        verify(userDao).get(email);
        verify(userDao, never()).save(any(User.class));
        assertEquals("Existing linked user is returned", user, result);
    }
}
