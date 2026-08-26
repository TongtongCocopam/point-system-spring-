package kongju.pointsystem.domain.user.repository;

import java.util.UUID;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.user.entity.UserBalance;


public interface UserBalanceRepository extends JpaRepository<UserBalance, UUID> {
    UserBalance findByUserId(UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from UserBalance b where b.user.id = :userId")
    Optional<UserBalance> findByUserIdWithLock(@Param("userId") UUID userId);

}
