package kongju.pointsystem.domain.point.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;


public record PointRequest(
        @NotNull(message = "{valid.id.required}")
        UUID id,
        @NotNull
        @Positive(message = "{valid.point.positive}")
        Long point
) {
}

