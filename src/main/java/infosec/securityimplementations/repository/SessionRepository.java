package infosec.securityimplementations.repository;

import infosec.securityimplementations.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Optional<Session> findBySessionToken(String sessionToken);

    List<Session> findByUserIdAndActiveTrue(Long userId);

    void deleteByUserId(Long userId);
}
