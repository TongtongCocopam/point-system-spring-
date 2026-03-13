package kongju.pointsystem.domain.user.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;

public record UserCreateResponse(
        @NotNull
        @Column(name = "아이디")
        String email,
        @Column(name = "메시지")
        String massage
) {
}
