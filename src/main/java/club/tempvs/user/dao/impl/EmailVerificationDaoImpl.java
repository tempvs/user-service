package club.tempvs.user.dao.impl;

import club.tempvs.user.dao.EmailVerificationDao;
import club.tempvs.user.domain.EmailVerification;
import club.tempvs.user.repository.EmailVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailVerificationDaoImpl implements EmailVerificationDao {

    private final EmailVerificationRepository emailVerificationRepository;

    @Override
    public Optional<EmailVerification> get(String id) {
        return emailVerificationRepository.findByVerificationId(id);
    }

    @Override
    public void delete(EmailVerification emailVerification) {
        emailVerificationRepository.delete(emailVerification);
    }

    @Override
    public EmailVerification save(EmailVerification emailVerification) {
        return emailVerificationRepository.save(emailVerification);
    }
}
