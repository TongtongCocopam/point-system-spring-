package kongju.pointsystem.domain.point.dto;

import java.util.UUID;


public record RefundRequest(
        UUID id,
        UUID referenceId
) {}
