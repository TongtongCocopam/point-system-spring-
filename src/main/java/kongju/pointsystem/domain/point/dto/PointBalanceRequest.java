package kongju.pointsystem.domain.point.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record PointBalanceRequest (
        @NotNull
        UUID id,
        @Nullable
        LocalDateTime time
){}
