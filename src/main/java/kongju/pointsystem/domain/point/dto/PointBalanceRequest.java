package kongju.pointsystem.domain.point.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.UUID;

public record PointBalanceRequest (
        @NotNull
        UUID id,
        @Nullable
        @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSSSSS")
        LocalDateTime time
){}
