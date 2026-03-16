package kongju.pointsystem.domain.point.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public record PointRefundResponse(
        @JsonProperty("환불 금액")
        Long refundAmount,
        @JsonProperty("만료된 금액")
        Long expiredAmount,
        @JsonProperty("현재 잔액")
        Long currentBalance,
        @JsonProperty("처리 결과")
        String message
) {
}
