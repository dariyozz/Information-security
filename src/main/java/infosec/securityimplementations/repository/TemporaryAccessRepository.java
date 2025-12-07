package infosec.securityimplementations.repository;

import infosec.securityimplementations.entity.TemporaryAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TemporaryAccessRepository extends JpaRepository<TemporaryAccess, Long> {

    List<TemporaryAccess> findByUserIdAndRevokedFalse(Long userId);

    Optional<TemporaryAccess> findByUserIdAndResourceIdAndRevokedFalse(Long userId, String resourceId);

    @Query("SELECT ta FROM TemporaryAccess ta WHERE ta.expiresAt < :now AND ta.revoked = false")
    List<TemporaryAccess> findExpiredAccess(LocalDateTime now);
}
