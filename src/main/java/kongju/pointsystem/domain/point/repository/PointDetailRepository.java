package kongju.pointsystem.domain.point.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.point.entity.PointDetail;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PointDetailRepository extends JpaRepository<PointDetail, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PointDetail> findByUserId(UUID userId);

    @Query("SELECT pd FROM PointHistory ph " +
            "JOIN PointDetail pd ON ph.user.id = pd.user.id " +
            "WHERE ph.user.id = :userId " +
            "and pd.expiredAt <= :time " +
            "and pd.remainAmount > 0 "
    )
    List<PointDetail> findByPointExpire(@Param("userId") UUID userId, @Param("time") LocalDateTime time);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT pd FROM PointDetail pd " +
            "WHERE pd.user.id = :userId " +
            "and pd.expiredAt > :now " +
            "and pd.remainAmount > 0 " +
            "ORDER BY pd.expiredAt ASC "
    )
    List<PointDetail> findRemainedDetailsNotExpired(@Param("userId") UUID userId, @Param("now") LocalDateTime now);
}
