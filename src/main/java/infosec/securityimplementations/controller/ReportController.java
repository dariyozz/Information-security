package infosec.securityimplementations.controller;

import infosec.securityimplementations.dto.ApiResponse;
import infosec.securityimplementations.repository.TemporaryAccessRepository;
import infosec.securityimplementations.repository.UserRepository;
import infosec.securityimplementations.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class ReportController {

    private final UserRepository userRepository;
    private final TemporaryAccessRepository temporaryAccessRepository;
    private final SessionService sessionService;

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats(
            @CookieValue(value = "SESSION_TOKEN", required = false) String sessionToken) {

        if (sessionService.validateSession(sessionToken).isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid session"));
        }

        long totalUsers = userRepository.count();
        long totalAccessRequests = temporaryAccessRepository.count();
        // Just basic stats for now

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("totalAccessRequests", totalAccessRequests);
        stats.put("activeSessions", 0); // Placeholder or implement if SessionRepository allows counting

        return ResponseEntity.ok(ApiResponse.success("Stats retrieved", stats));
    }
}
