package infosec.securityimplementations.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "temporary_access")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemporaryAccess {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 100)
    private String resourceId;

    @Column(nullable = false, length = 50)
    private String resourceType;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private Integer durationMinutes;

    @Column(updatable = false)
    private LocalDateTime grantedAt;

    @Column
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean revoked = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessStatus status = AccessStatus.PENDING;

    @PrePersist
    protected void onCreate() {
        grantedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isActive() {
        return !revoked && !isExpired() && status == AccessStatus.APPROVED;
    }
}
