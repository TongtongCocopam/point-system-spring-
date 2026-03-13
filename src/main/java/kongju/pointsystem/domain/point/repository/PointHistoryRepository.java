package kongju.pointsystem.domain.point.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import kongju.pointsystem.domain.point.entity.PointUsage;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.point.entity.PointHistory;
import org.springframework.data.repository.query.Param;


public interface PointHistoryRepository extends JpaRepository<PointHistory, UUID> {
    @Query("SELECT COUNT(ph) > 0 " +
            "FROM PointHistory ph " +
            "WHERE ph.user.id = :userId " +
            "AND ph.referenceId = :referenceId")
    boolean existsReferenceId(@Param("userId") UUID userId, @Param("referenceId") UUID referenceId);

    @Query("SELECT COUNT(ph) > 0 " +
            "FROM PointHistory ph " +
            "WHERE ph.user.id = :userId " +
            "AND ph.referenceId = :referenceId " +
            "AND ph.type = 'REFUND'")
    boolean findRefundPoints(@Param("userId") UUID userId, @Param("referenceId") UUID referenceId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT DISTINCT ph " +
            "FROM PointHistory ph " +
            "JOIN FETCH ph.pointUsages pu " +
            "JOIN FETCH pu.pointDetail pd " +
            "WHERE ph.referenceId = :referenceId " +
            "AND ph.user.id = :userId " +
            "AND ph.type = 'USE' ")
    PointHistory findRefundablePoints(@Param("userId") UUID userId, @Param("referenceId") UUID referenceId);
}
