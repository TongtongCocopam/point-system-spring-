package kongju.pointsystem.domain.user.dto;

import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

@Builder
public record UserCreateResponse(
        @NotNull
        @Column(name = "아이디")
        String email,
        @Column(name = "메시지")
        String message
) {
}
