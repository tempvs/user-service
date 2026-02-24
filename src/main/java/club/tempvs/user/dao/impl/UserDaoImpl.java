package club.tempvs.user.dao.impl;

import club.tempvs.user.dao.UserDao;
import club.tempvs.user.domain.User;
import club.tempvs.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> get(String email) {
        return userRepository.findByEmailIgnoreCase(email);
    }
}
