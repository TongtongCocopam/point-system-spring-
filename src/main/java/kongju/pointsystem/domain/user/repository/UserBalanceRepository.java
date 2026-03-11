package kongju.pointsystem.domain.user.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.user.entity.UserBalance;


public interface UserBalanceRepository extends JpaRepository<UserBalance, UUID> {
    Optional<UserBalance> findByUserId(UUID userId);
}
