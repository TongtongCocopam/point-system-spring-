package kongju.pointsystem.domain.point.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public record PointEarnResponse(
        @JsonProperty("적립 금액")
        Long earnedAmount,
        @JsonProperty("현재 잔액")
        Long currentBalance,
        @JsonProperty("처리 결과")
        String message
) {
}
