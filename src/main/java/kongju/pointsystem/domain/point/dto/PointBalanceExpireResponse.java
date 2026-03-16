package kongju.pointsystem.domain.point.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonProperty;


@Builder
public record PointBalanceExpireResponse(
        @JsonProperty("잔액")
        Long balance,
        @JsonProperty("만료 예정일")
        LocalDateTime expiredAt
) implements PointResponse {
}
