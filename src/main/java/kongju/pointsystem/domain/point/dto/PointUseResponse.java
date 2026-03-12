package kongju.pointsystem.domain.point.dto;

import jakarta.persistence.Column;
import lombok.Builder;

@Builder
public record PointUseResponse(
        @Column(name = "사용 금액")
        Long useAmount,
        @Column(name = "현재 잔액")
        Long currentBalance
) {
}
