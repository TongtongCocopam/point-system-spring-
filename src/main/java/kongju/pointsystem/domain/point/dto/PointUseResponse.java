package kongju.pointsystem.domain.point.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public record PointUseResponse(
        @JsonProperty("사용 금액")
        Long useAmount,
        @JsonProperty("현재 잔액")
        Long currentBalance
) {
}
