package infosec.securityimplementations.service;

import infosec.securityimplementations.entity.Session;
import infosec.securityimplementations.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private static final SecureRandom random = new SecureRandom();

    @Value("${session.timeout.minutes:30}")
    private int sessionTimeoutMinutes;

    /**
     * Generate a secure random session token
     */
    public String generateSessionToken() {
        byte[] tokenBytes = new byte[32];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    /**
     * Create a new session for a user
     */
    @Transactional
    public String createSession(Long userId) {
        // Invalidate any existing active sessions for this user
        sessionRepository.findByUserIdAndActiveTrue(userId)
                .forEach(session -> {
                    session.setActive(false);
                    sessionRepository.save(session);
                });

        // Create new session
        String sessionToken = generateSessionToken();
        Session session = Session.builder()
                .userId(userId)
                .sessionToken(sessionToken)
                .expiresAt(LocalDateTime.now().plusMinutes(sessionTimeoutMinutes))
                .active(true)
                .build();

        sessionRepository.save(session);
        return sessionToken;
    }

    /**
     * Validate a session token and return the user ID if valid
     */
    public Optional<Long> validateSession(String sessionToken) {
        if (sessionToken == null || sessionToken.isEmpty()) {
            return Optional.empty();
        }

        Optional<Session> optionalSession = sessionRepository.findBySessionToken(sessionToken);

        if (optionalSession.isEmpty()) {
            return Optional.empty();
        }

        Session session = optionalSession.get();

        // Check if session is active
        if (!session.getActive()) {
            return Optional.empty();
        }

        // Check if session is expired
        if (LocalDateTime.now().isAfter(session.getExpiresAt())) {
            session.setActive(false);
            sessionRepository.save(session);
            return Optional.empty();
        }

        return Optional.of(session.getUserId());
    }

    /**
     * Invalidate a session (logout)
     */
    @Transactional
    public void invalidateSession(String sessionToken) {
        sessionRepository.findBySessionToken(sessionToken)
                .ifPresent(session -> {
                    session.setActive(false);
                    sessionRepository.save(session);
                });
    }

    /**
     * Invalidate all sessions for a user
     */
    @Transactional
    public void invalidateAllUserSessions(Long userId) {
        sessionRepository.findByUserIdAndActiveTrue(userId)
                .forEach(session -> {
                    session.setActive(false);
                    sessionRepository.save(session);
                });
    }
}
