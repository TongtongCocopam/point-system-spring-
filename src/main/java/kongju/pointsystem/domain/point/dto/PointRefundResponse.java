package kongju.pointsystem.domain.point.dto;

import jakarta.persistence.Column;
import lombok.Builder;

@Builder
public record PointRefundResponse(
        @Column(name = "환불 금액")
        Long refundAmount,
        @Column(name = "만료된 금액")
        Long expiredAmount,
        @Column(name = "현재 잔액")
        Long currentBalance,
        @Column(name = "처리 결과")
        String message
) {
}
