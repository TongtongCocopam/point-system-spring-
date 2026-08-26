package kongju.pointsystem.domain.point.repository;

import java.util.List;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import kongju.pointsystem.domain.point.entity.PointDetail;


public interface PointDetailRepository extends JpaRepository<PointDetail, UUID> {
    @Query("""
                SELECT pd
                FROM PointDetail pd
                WHERE pd.user.id = :userId
                  AND pd.expiredAt > :now
                  AND pd.expiredAt <= :expireTime
                  AND pd.remainAmount > 0
            """
    )
    List<PointDetail> findExpiringPointsBetween(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now,
            @Param("expireTime") LocalDateTime expireTime
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

    @Query("""
                SELECT pd
                FROM PointDetail pd
                WHERE pd.user.id = :userId
                  AND pd.expiredAt <= :now
                  AND pd.remainAmount > 0
            """)
    List<PointDetail> findRemainedDetailsExpired(
            @Param("userId") UUID userId,
            @Param("now") LocalDateTime now
    );


    @Query("""
            SELECT DISTINCT pd.user.id
            FROM PointDetail pd
            WHERE pd.expiredAt <= :now
              AND pd.remainAmount > 0
            """)
    List<UUID> findUsersWithExpiredPoints(
            @Param("now") LocalDateTime now
    );
}
