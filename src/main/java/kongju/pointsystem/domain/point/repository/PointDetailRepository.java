package kongju.pointsystem.domain.point.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import kongju.pointsystem.domain.point.entity.PointType;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.point.entity.PointDetail;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface PointDetailRepository extends JpaRepository<PointDetail, UUID> {

    @Query("SELECT pd FROM PointHistory ph " +
            "JOIN PointDetail pd ON ph.user.id = pd.user.id " +
            "WHERE ph.user.id = :userId " +
            "and pd.expiredAt <= :time " +
            "and pd.remainAmount > 0 "
    )
    List<PointDetail> findByPointExpire(
            @Param("userId") UUID userId,
            @Param("time") LocalDateTime time
    );

    @Query("SELECT pd FROM PointDetail pd " +
            "WHERE pd.user.id = :userId " +
            "and pd.expiredAt > :now " +
            "and pd.remainAmount > 0 " +
            "ORDER BY pd.expiredAt ASC "
    )
    List<PointDetail> findRemainedDetailsNotExpired(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now
    );

    @Query("SELECT pd FROM PointDetail pd " +
            "WHERE pd.expiredAt <= :now " +
            "AND NOT EXISTS (" +
            "    SELECT pu FROM PointUsage pu " +
            "    WHERE pu.pointDetail = pd " +
            "    AND pu.pointHistory.type = :type " +
            "    AND pd.remainAmount > 0" +
            ")")
    List<PointDetail> findRemainedDetailsExpired(
            @Param("now") LocalDateTime now,
            @Param("type") PointType type
    );
}
