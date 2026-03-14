package club.tempvs.user.service.impl;

import club.tempvs.user.dao.EmailVerificationDao;
import club.tempvs.user.dao.UserDao;
import club.tempvs.user.domain.EmailVerification;
import club.tempvs.user.domain.User;
import club.tempvs.user.exception.UnauthorizedException;
import club.tempvs.user.exception.UserAlreadyExistsException;
import club.tempvs.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationDao emailVerificationDao;

    @Override
    @Transactional
    public User register(String verificationId, String password) {
        EmailVerification emailVerification = emailVerificationDao.get(verificationId)
                .orElseThrow(NoSuchElementException::new);
        String email = emailVerification.getEmail();
        emailVerificationDao.delete(emailVerification);

        if (userDao.get(email).isPresent()) {
            throw new UserAlreadyExistsException(email);
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(email, encodedPassword);
        return userDao.save(user);
    }

    @Override
    @Transactional
    public User login(String email, String password) {
        User user = userDao.get(email)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Wrong credentials");
        }

        return user;
    }

    @Override
    @Transactional
    public User createExternalUser(String externalId, String email) {
        return userDao.getByExternalId(externalId)
                .orElseGet(() -> createOrLinkUserByEmail(externalId, email));
    }

    private User createOrLinkUserByEmail(String externalId, String email) {
        User user = resolveUserByEmail(email);
        if (StringUtils.hasText(user.getExternalId())) {
            return user;
        }

        user.setExternalId(externalId);
        return userDao.save(user);
    }

    private User resolveUserByEmail(String email) {
        if (!StringUtils.hasText(email)) {
            return new User();
        }

        return userDao.get(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    return newUser;
                });
    }
}
