package kongju.pointsystem.domain.point.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.point.entity.PointDetail;
import org.springframework.data.jpa.repository.Lock;


public interface PointDetailRepository extends JpaRepository<PointDetail, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PointDetail> findByUserId(UUID userId);
}
