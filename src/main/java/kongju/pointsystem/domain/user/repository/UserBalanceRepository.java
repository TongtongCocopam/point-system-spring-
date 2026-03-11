package kongju.pointsystem.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.user.entity.UserBalance;


public interface UserBalanceRepository extends JpaRepository<UserBalance, UUID> {
    Optional<UserBalance> findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserBalance> findByUserIdWithLock(@Param("userId") UUID userId);

}
