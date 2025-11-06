package com.infobez.lab.models;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password; // Хеширана лозинка

    @Column(nullable = false)
    private boolean enabled = false; // false се додека не се верификува email

    // 2FA полиња
    @Column(name = "verification_code")
    private String verificationCode; // Код за верификација

    @Column(name = "verification_code_expiry")
    private LocalDateTime verificationCodeExpiry; // Истекување на кодот

    @Column(name = "two_factor_enabled")
    private boolean twoFactorEnabled = true; // Дали е овозможена 2FA

    @Column(name = "two_factor_code")
    private String twoFactorCode; // Код за 2FA при најава

    @Column(name = "two_factor_code_expiry")
    private LocalDateTime twoFactorCodeExpiry; // Истекување на 2FA кодот

    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.enabled = false;
        this.twoFactorEnabled = true;
    }
}