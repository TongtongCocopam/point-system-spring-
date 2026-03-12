package kongju.pointsystem.domain.point.repository;

import kongju.pointsystem.domain.point.entity.PointUsage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PointUsageRepository  extends JpaRepository<PointUsage, UUID> {

}
