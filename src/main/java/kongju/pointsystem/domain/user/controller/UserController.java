package kongju.pointsystem.domain.user.controller;

import jakarta.validation.Valid;
import kongju.pointsystem.domain.user.dto.UserCreateRequest;
import kongju.pointsystem.domain.user.dto.UserCreateResponse;
import kongju.pointsystem.domain.user.service.UserService;
import kongju.pointsystem.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserCreateResponse>> createUser(@Valid @RequestBody UserCreateRequest request) {
        UserCreateResponse userCreateResponse = userService.createUser(
                request.email(),
                request.name(),
                request.password()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(userCreateResponse));
    }
}
