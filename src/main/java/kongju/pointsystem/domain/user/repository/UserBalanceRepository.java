package kongju.pointsystem.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.user.entity.UserBalance;


public interface UserBalanceRepository extends JpaRepository<UserBalance, UUID> {
    UserBalance findByUserId(UUID userId);

    @Query("select b from UserBalance b where b.user.id = :userId")
    Optional<UserBalance> findByUserIdWithLock(@Param("userId") UUID userId);

}
