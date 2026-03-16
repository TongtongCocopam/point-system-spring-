package kongju.pointsystem.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
        @NotBlank(message = "{valid.email.required}")
        @Email(message = "{valid.email.format}")
        String email,
        @NotBlank(message = "{valid.password.required}")
        @Size(min = 8, message = "{valid.password.size}")
        String password,
        @NotBlank(message = "{valid.required}")
        String name
) {
}
