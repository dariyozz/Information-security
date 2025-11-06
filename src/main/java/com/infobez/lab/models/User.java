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
    private boolean enabled = true; // Дали корисникот е активен


    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}