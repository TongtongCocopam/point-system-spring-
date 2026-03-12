package kongju.pointsystem.domain.point.dto;

import java.util.UUID;

public record PointRequest(
        UUID id,
        Long point
) {
}

