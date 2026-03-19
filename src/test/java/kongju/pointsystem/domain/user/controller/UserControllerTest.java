package kongju.pointsystem.domain.user.controller;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fasterxml.jackson.databind.ObjectMapper;

import kongju.pointsystem.domain.user.dto.UserCreateRequest;
import kongju.pointsystem.domain.user.dto.UserCreateResponse;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.domain.user.service.UserService;
import kongju.pointsystem.global.error.ErrorCode;
import kongju.pointsystem.global.error.exception.EmailDuplicatedException;


@WebMvcTest(controllers = UserController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserService userService;

    private final ObjectMapper objectMapper = new ObjectMapper();



    @Test
    @DisplayName("POST /api/v1/users/register - 가입 성공")
    void register_success() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@gmail.com")
                .name("tester")
                .password("test1234")
                .build();

        UserCreateResponse response = UserCreateResponse.builder()
                .email("test@gmail.com")
                .message("회원가입 성공")
                .build();

        when(userService.createUser(any(UserCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data['아이디']").value("test@gmail.com"))
                .andExpect(jsonPath("$.data['메시지']").value("회원가입 성공"))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/users/register - 가입 실패 : 필수 필드 누락")
    void register_fail() throws Exception {
        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@gmail.com")
                .name(null)
                .password("test1234")
                .build();

        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value("{valid.required}"));
    }

    @Test
    @DisplayName("POST /api/v1/users/register - 가입 실패 : 이메일 중복")
    void register_fail_email_duplicated() throws Exception {

        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@gmail.com")
                .name("tester")
                .password("test1234")
                .build();

        ErrorCode errorCode = ErrorCode.DUPLICATE_DATA;

        when(userService.createUser(any()))
                .thenThrow(new EmailDuplicatedException());

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value(errorCode.getMessage()));
    }

}
