package kongju.pointsystem.domain.point.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;


@Builder
public record PointBalanceResponse(
        @JsonProperty("잔액")
        Long balance
) implements PointResponse {

}
