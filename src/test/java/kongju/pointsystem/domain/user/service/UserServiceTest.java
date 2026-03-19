package kongju.pointsystem.domain.user.service;

import kongju.pointsystem.domain.user.dto.UserCreateRequest;
import kongju.pointsystem.domain.user.dto.UserCreateResponse;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    @Test
    @DisplayName("올바르게 가입된 경우")
    void should_register_success_when_user_registered() {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@gmail.com")
                .name("tester")
                .password("test1234")
                .build();

        User mockUser = User.builder()
                .email("test@gmail.com")
                .name("tester")
                .password("encoded_password")
                .build();

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserCreateResponse response = userService.createUser(request);

        assertThat(response.email()).isEqualTo(request.email());
        assertThat(response.message()).isEqualTo("회원가입이 완료되었습니다");

    }


}
