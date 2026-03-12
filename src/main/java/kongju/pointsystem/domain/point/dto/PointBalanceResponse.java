package kongju.pointsystem.domain.point.dto;

import jakarta.persistence.Column;
import lombok.Builder;

@Builder
public record PointBalanceResponse(
        @Column(name = "잔액")
        Long balance
) implements PointResponse {

}
