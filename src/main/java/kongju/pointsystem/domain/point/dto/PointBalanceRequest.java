package kongju.pointsystem.domain.point.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;


public record PointBalanceRequest(
        @NotNull(message = "{valid.id.required}")
        UUID id,
        @Nullable
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime time
) {
}
