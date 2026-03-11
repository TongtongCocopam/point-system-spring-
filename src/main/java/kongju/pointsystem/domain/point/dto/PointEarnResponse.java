package kongju.pointsystem.domain.point.dto;

import lombok.Builder;

@Builder
public record PointEarnResponse(
        Long earnedAmount,
        Long currentBalance,
        String message
) {
}
