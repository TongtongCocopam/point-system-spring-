package kongju.pointsystem.domain.point.repository;

import java.util.UUID;

import kongju.pointsystem.domain.point.entity.PointUsage;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PointUsageRepository extends JpaRepository<PointUsage, UUID> {

}
