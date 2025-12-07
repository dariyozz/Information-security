package infosec.securityimplementations.repository;

import infosec.securityimplementations.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByUserIdAndTypeAndUsedFalse(Long userId, VerificationCode.CodeType type);

    Optional<VerificationCode> findByCodeAndUsedFalse(String code);
}
