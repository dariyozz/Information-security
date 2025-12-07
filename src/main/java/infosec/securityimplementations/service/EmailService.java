package infosec.securityimplementations.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Email service that simulates sending emails by outputting to console
 * This avoids the need for SMTP configuration during development/testing
 */
@Service
@Slf4j
public class EmailService {

    /**
     * Simulate sending verification code email by printing to console
     */
    void sendVerificationCode(String email, String code, String purpose) {
        String border = "═".repeat(60);
        log.info(border);
        log.info("📧 EMAIL SIMULATION - {}", purpose);
        log.info(border);
        log.info("To: {}", email);
        log.info("Subject: Your Verification Code");
        log.info("");
        log.info("🔐 Your verification code is: {}", code);
        log.info("");
        log.info("⏱️ This code will expire in 10 minutes.");
        log.info(border);
    }

    /**
     * Send email verification code
     */
    public void sendEmailVerificationCode(String email, String code) {
        sendVerificationCode(email, code, "EMAIL VERIFICATION");
    }

    /**
     * Send 2FA code
     */
    public void send2FACode(String email, String code) {
        sendVerificationCode(email, code, "TWO-FACTOR AUTHENTICATION");
    }
}
