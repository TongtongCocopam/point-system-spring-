package kongju.pointsystem.domain.point.dto;

import jakarta.persistence.Column;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PointBalanceExpireResponse(
        @Column(name = "잔액")
        Long balance,
        @Column(name = "만료 예정일")
        LocalDateTime expiredAt
) implements PointResponse {
}
