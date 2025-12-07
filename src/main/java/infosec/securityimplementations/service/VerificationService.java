package infosec.securityimplementations.service;

import infosec.securityimplementations.entity.VerificationCode;
import infosec.securityimplementations.repository.VerificationCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationCodeRepository verificationCodeRepository;
    private final EmailService emailService;
    private static final SecureRandom random = new SecureRandom();
    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRY_MINUTES = 10;

    /**
     * Generate a random numeric verification code
     */
    public String generateCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * Create and store a verification code for a user
     */
    @Transactional
    public String createVerificationCode(Long userId, VerificationCode.CodeType type) {
        String code = generateCode();

        VerificationCode verificationCode = VerificationCode.builder()
                .userId(userId)
                .code(code)
                .type(type)
                .expiresAt(LocalDateTime.now().plusMinutes(CODE_EXPIRY_MINUTES))
                .used(false)
                .build();

        verificationCodeRepository.save(verificationCode);
        return code;
    }

    /**
     * Validate a verification code
     */
    @Transactional
    public boolean validateCode(Long userId, String code, VerificationCode.CodeType type) {
        Optional<VerificationCode> optionalCode = verificationCodeRepository
                .findByUserIdAndTypeAndUsedFalse(userId, type);

        if (optionalCode.isEmpty()) {
            return false;
        }

        VerificationCode verificationCode = optionalCode.get();

        // Check if code matches
        if (!verificationCode.getCode().equals(code)) {
            return false;
        }

        // Check if code is expired
        if (LocalDateTime.now().isAfter(verificationCode.getExpiresAt())) {
            return false;
        }

        // Mark code as used
        verificationCode.setUsed(true);
        verificationCodeRepository.save(verificationCode);

        return true;
    }

    /**
     * Create and send email verification code
     */
    public String sendEmailVerificationCode(Long userId, String email) {
        String code = createVerificationCode(userId, VerificationCode.CodeType.EMAIL_VERIFICATION);
        emailService.sendEmailVerificationCode(email, code);
        return code;
    }

    /**
     * Create and send 2FA code
     */
    public String send2FACode(Long userId, String email) {
        String code = createVerificationCode(userId, VerificationCode.CodeType.TWO_FACTOR);
        emailService.send2FACode(email, code);
        return code;
    }
}
