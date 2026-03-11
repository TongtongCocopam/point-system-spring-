package kongju.pointsystem.domain.point.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.point.entity.PointHistory;
import org.springframework.data.repository.query.Param;


public interface PointHistoryRepository extends JpaRepository<PointHistory, UUID> {
    Optional<PointHistory> findByUserId(UUID userId);

    List<PointHistory> findByUserIdAndReferenceId(UUID userId, long referenceId);

    @Query("SELECT ph FROM PointHistory ph " +
            "WHERE ph.user.id = :userId " +
            "and ph.referenceId = :referenceId " +
            "and ph.type = 'REFUND' ")
    boolean findRefundPoints(@Param("userId") UUID userId, @Param("referenceId") long referenceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ph FROM PointHistory ph " +
            "JOIN PointDetail pd ON ph.user.id = pd.user.id " +
            "WHERE ph.user.id = :userId " +
            "and ph.referenceId = :referenceId " +
            "and ph.type = 'USE' " +
            "AND pd.expiredAt > CURRENT_TIMESTAMP " +
            "ORDER BY pd.expiredAt ASC")
    List<PointHistory> findRefundablePoints(@Param("userId") UUID userId, @Param("referenceId") long referenceId);

}
