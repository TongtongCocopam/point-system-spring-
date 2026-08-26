package kongju.pointsystem.domain.point.dto;

import java.util.UUID;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import org.springframework.format.annotation.DateTimeFormat;


public record PointBalanceRequest(
        @NotNull(message = "{valid.id.required}")
        UUID id,
        @FutureOrPresent(message = "조회 날짜는 현재 이후여야 합니다.")
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime time
) {
}
