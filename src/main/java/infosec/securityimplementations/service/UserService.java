package infosec.securityimplementations.service;

import infosec.securityimplementations.entity.User;
import infosec.securityimplementations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final AuthorizationService authorizationService;
    private final SessionService sessionService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public Map<String, Object> blockUser(Long userId, Long adminId) {
        Map<String, Object> result = new HashMap<>();

        if (!authorizationService.hasRole(adminId, "ADMIN")) {
            result.put("success", false);
            result.put("message", "Permission denied");
            return result;
        }

        if (userId.equals(adminId)) {
            result.put("success", false);
            result.put("message", "Cannot block yourself");
            return result;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }

        User user = userOpt.get();
        user.setBlocked(true);
        userRepository.save(user);

        // Invalidate all sessions for this user
        sessionService.invalidateAllUserSessions(userId);

        result.put("success", true);
        result.put("message", "User blocked successfully");
        return result;
    }

    @Transactional
    public Map<String, Object> unblockUser(Long userId, Long adminId) {
        Map<String, Object> result = new HashMap<>();

        if (!authorizationService.hasRole(adminId, "ADMIN")) {
            result.put("success", false);
            result.put("message", "Permission denied");
            return result;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            result.put("success", false);
            result.put("message", "User not found");
            return result;
        }

        User user = userOpt.get();
        user.setBlocked(false);
        userRepository.save(user);

        result.put("success", true);
        result.put("message", "User unblocked successfully");
        return result;
    }
}
