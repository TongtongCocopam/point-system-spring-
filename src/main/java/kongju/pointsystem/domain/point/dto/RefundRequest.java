package kongju.pointsystem.domain.point.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;


public record RefundRequest(
        @NotNull(message = "{valid.id.required}")
        UUID id,
        @NotNull(message = "{valid.required}")
        UUID referenceId
) {}
