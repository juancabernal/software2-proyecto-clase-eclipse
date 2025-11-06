package co.edu.uco.ucochallenge.application.user.contactconfirmation.service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import co.edu.uco.ucochallenge.crosscutting.MessageCodes;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.BusinessException;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.DomainValidationException;
import co.edu.uco.ucochallenge.crosscutting.legacy.exception.NotFoundException;
import co.edu.uco.ucochallenge.crosscutting.ParamKeys;
import co.edu.uco.ucochallenge.crosscutting.dto.ParameterDTO;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.UserEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.entity.VerificationCodeEntity;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.jpa.SpringDataUserRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.repository.jpa.VerificationCodeRepository;
import co.edu.uco.ucochallenge.infrastructure.secondary.adapters.cache.catalog.ParametersCatalogCache;
import co.edu.uco.ucochallenge.application.user.contactconfirmation.port.ConfirmUserContactRepositoryPort;

@Service
public class UserContactConfirmationServiceImpl implements UserContactConfirmationService {

    private static final Pattern CODE_PATTERN = Pattern.compile("^\\d{6}$");
    private static final int DEFAULT_MAX_ATTEMPTS = 3;

    private final VerificationCodeRepository codeRepo;
    private final SpringDataUserRepository userRepo;
    private final ParametersCatalogCache parametersCatalogCache;
    private final ConfirmUserContactRepositoryPort confirmUserContactRepository;

    public UserContactConfirmationServiceImpl(final VerificationCodeRepository codeRepo,
            final SpringDataUserRepository userRepo,
            final ParametersCatalogCache parametersCatalogCache,
            final ConfirmUserContactRepositoryPort confirmUserContactRepository) {
        this.codeRepo = codeRepo;
        this.userRepo = userRepo;
        this.parametersCatalogCache = parametersCatalogCache;
        this.confirmUserContactRepository = confirmUserContactRepository;
    }

    @Transactional
    @Override
    public void confirmVerificationCode(final UUID userId, final VerificationChannel channel, final String rawCode) {
        final UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(MessageCodes.VERIFICATION_USER_NOT_FOUND));

        final String contact = resolveContact(user, channel);
        final String normalizedContact = channel.normalizeContact(contact);
        final String code = rawCode == null ? "" : rawCode.trim();

        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new DomainValidationException(MessageCodes.VERIFICATION_CODE_FORMAT_INVALID);
        }

        final VerificationCodeEntity verificationCode = codeRepo.findByContactIgnoreCase(normalizedContact)
                .orElseThrow(() -> new BusinessException(MessageCodes.VERIFICATION_CODE_NOT_FOUND));

        final Integer attemptsValue = parametersCatalogCache.getParameter(ParamKeys.MAX_CONFIRM_ATTEMPTS)
                .map(ParameterDTO::value)
                .map(String::trim)
                .map(Integer::parseInt)
                .onErrorReturn(DEFAULT_MAX_ATTEMPTS)
                .defaultIfEmpty(DEFAULT_MAX_ATTEMPTS)
                .block();
        final int maxAttempts = attemptsValue == null || attemptsValue <= 0 ? DEFAULT_MAX_ATTEMPTS : attemptsValue;

        if (verificationCode.getAttempts() >= maxAttempts) {
            throw new BusinessException(MessageCodes.VERIFICATION_CODE_MAX_ATTEMPTS);
        }

        if (LocalDateTime.now().isAfter(verificationCode.getExpiration())) {
            codeRepo.deleteByContactIgnoreCase(normalizedContact);
            throw new BusinessException(MessageCodes.VERIFICATION_CODE_EXPIRED);
        }

        if (!verificationCode.getCode().equals(code)) {
            verificationCode.incrementAttempts();
            codeRepo.save(verificationCode);
            throw new BusinessException(MessageCodes.VERIFICATION_CODE_INVALID);
        }

        if (channel.isEmail()) {
            confirmUserContactRepository.confirmEmail(userId);
        } else {
            confirmUserContactRepository.confirmMobileNumber(userId);
        }

        codeRepo.deleteByContactIgnoreCase(normalizedContact);
    }

    private String resolveContact(final UserEntity user, final VerificationChannel channel) {
        final String contact = channel.isEmail() ? user.getEmail() : user.getMobileNumber();
        if (contact == null || contact.trim().isEmpty()) {
            throw new BusinessException(channel.isEmail()
                    ? MessageCodes.VERIFICATION_CONTACT_EMAIL_MISSING
                    : MessageCodes.VERIFICATION_CONTACT_MOBILE_MISSING);
        }
        return contact.trim();
    }
}
