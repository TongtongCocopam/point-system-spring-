package kongju.pointsystem.domain.user.service;

import kongju.pointsystem.domain.user.dto.UserCreateRequest;
import kongju.pointsystem.domain.user.dto.UserCreateResponse;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.global.error.ErrorCode;
import kongju.pointsystem.global.error.exception.EmailDuplicatedException;
import kongju.pointsystem.global.error.exception.EmptyFieldException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

    @ParameterizedTest
    @CsvSource(
            value = {
                    "NULL, tester, test1234",
                    "test@gmail.com, NULL, test1234",
                    "test@gmail.com, tester, NULL",
                    "'', tester, test1234",
                    "test@gmail.com, '', test1234",
                    "test@gmail.com, tester, ''",
                    "'   ', tester, test1234",
                    "test@gmail.com, '   ', test1234",
                    "test@gmail.com, tester, '   '"
            },
            nullValues = "NULL"
    )
    @DisplayName("필수 필드가 null 또는 공백이면 회원가입 실패")
    void should_register_fail_when_field_empty(
            String email,
            String name,
            String password
    ) {
        UserCreateRequest request = UserCreateRequest.builder()
                .email(email)
                .name(name)
                .password(password)
                .build();

        EmptyFieldException exception = assertThrows(
                EmptyFieldException.class,
                () -> userService.createUser(request)
        );

        assertThat(exception.getMessage()).isEqualTo(ErrorCode.EMPTY_FIELD.getMessage());
    }


    @Test
    @DisplayName("이메일이 중복될 때")
    void should_register_fail_when_duplicated_email() {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@gmail.com")
                .name("tester")
                .password("test1234")
                .build();

        ErrorCode errorCode = ErrorCode.DUPLICATE_DATA;

        when(userRepository.existsByEmail(request.email())).thenReturn(true);
        EmailDuplicatedException exception = assertThrows(EmailDuplicatedException.class, () -> {
            userService.createUser(request);
        });

        assertThat(exception.getMessage()).isEqualTo(errorCode.getMessage());
    }


}
