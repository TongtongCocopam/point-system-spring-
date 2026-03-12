package kongju.pointsystem.domain.point.dto;

import jakarta.persistence.Column;
import lombok.Builder;

@Builder
public record PointEarnResponse(
        @Column(name = "적립 금액")
        Long earnedAmount,
        @Column(name = "현재 잔액")
        Long currentBalance,
        @Column(name = "처리 결과")
        String message
) {
}
