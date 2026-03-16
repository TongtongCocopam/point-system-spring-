package kongju.pointsystem.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import jakarta.validation.constraints.NotNull;

@Builder
public record UserCreateResponse(
        @NotNull
        @JsonProperty("아이디")
        String email,
        @JsonProperty("메시지")
        String message
) {
}
