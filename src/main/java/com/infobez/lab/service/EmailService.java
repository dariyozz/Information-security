package com.infobez.lab.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Email Service - Симулација на испраќање на email
 * Во продукција треба да се користи вистинска имплементација со JavaMailSender
 */
@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    /**
     * Испраќање на верификациски код на email
     */
    public void sendVerificationEmail(String email, String verificationCode) {
        // Симулација - во продукција овде би испратиле вистински email
        logger.info("========================================");
        logger.info("СИМУЛАЦИЈА НА EMAIL");
        logger.info("До: {}", email);
        logger.info("Тема: Верификација на акаунт");
        logger.info("Твојот верификациски код е: {}", verificationCode);
        logger.info("Кодот истекува за 10 минути.");
        logger.info("========================================");

        // За тестирање - испечати го кодот во конзола
        System.out.println("\n📧 EMAIL ДО: " + email);
        System.out.println("🔑 VERIFICATION CODE: " + verificationCode);
        System.out.println("⏰ Важи 10 минути\n");
    }

    /**
     * Испраќање на 2FA код при најава
     */
    public void sendTwoFactorCode(String email, String twoFactorCode) {
        logger.info("========================================");
        logger.info("СИМУЛАЦИЈА НА EMAIL");
        logger.info("До: {}", email);
        logger.info("Тема: Код за најава (2FA)");
        logger.info("Твојот 2FA код е: {}", twoFactorCode);
        logger.info("Кодот истекува за 5 минути.");
        logger.info("========================================");

        System.out.println("\n📧 EMAIL ДО: " + email);
        System.out.println("🔐 2FA CODE: " + twoFactorCode);
        System.out.println("⏰ Важи 5 минути\n");
    }

    /**
     * Испраќање на email за успешна регистрација
     */
    public void sendWelcomeEmail(String email, String username) {
        logger.info("========================================");
        logger.info("СИМУЛАЦИЈА НА EMAIL");
        logger.info("До: {}", email);
        logger.info("Тема: Добредојде!");
        logger.info("Здраво {}, успешно се регистрираше!", username);
        logger.info("========================================");

        System.out.println("\n📧 WELCOME EMAIL ДО: " + email);
        System.out.println("👋 Корисник: " + username + "\n");
    }
}