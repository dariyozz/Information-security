package com.infobez.lab.service;


import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

/**
 * Verification Service - Генерирање на верификациски кодови
 */
@Service
public class VerificationService {

    private static final SecureRandom random = new SecureRandom();
    private static final int CODE_LENGTH = 6;

    /**
     * Генерирање на 6-цифрен верификациски код
     */
    public String generateVerificationCode() {
        int code = 100000 + random.nextInt(900000); // 6-цифрен број (100000-999999)
        return String.valueOf(code);
    }

    /**
     * Генерирање на алфанумерички код (за посложени случаи)
     */
    public String generateAlphanumericCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < length; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }

        return code.toString();
    }

    /**
     * Проверка дали кодот е истечен
     */
    public boolean isCodeExpired(LocalDateTime expiryTime) {
        if (expiryTime == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(expiryTime);
    }

    /**
     * Генерирање на expiry time (10 минути од сега)
     */
    public LocalDateTime generateExpiryTime(int minutes) {
        return LocalDateTime.now().plusMinutes(minutes);
    }
}