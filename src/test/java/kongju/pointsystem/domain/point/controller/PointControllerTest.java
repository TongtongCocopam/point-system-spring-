package kongju.pointsystem.domain.point.controller;

import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import kongju.pointsystem.domain.point.dto.PointEarnResponse;
import kongju.pointsystem.domain.point.dto.PointRequest;
import kongju.pointsystem.domain.point.service.PointService;
import kongju.pointsystem.global.error.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PointController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
public class PointControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PointService pointService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @DisplayName("POST /api/v1/points/earn - 적립 성공")
    void earn_success() throws Exception {
        UUID userId = UUID.randomUUID();
        PointRequest request = new PointRequest(userId, 1000L);

        PointEarnResponse response = PointEarnResponse.builder()
                .earnedAmount(1000L)
                .currentBalance(2000L)
                .message("적립 성공")
                .build();

        when(pointService.earnPoint(any(PointRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/points/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data['적립 금액']").value(1000))
                .andExpect(jsonPath("$.data['현재 잔액']").value(2000))
                .andExpect(jsonPath("$.data['처리 결과']").value("적립 성공"))
                .andExpect(jsonPath("$.error").isEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/points/earn - 실패 : 잘못된 요청 금액")
    void earn_fail_invalid_amount() throws Exception {
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        PointRequest request = new PointRequest(UUID.randomUUID(), -1000L);

        mockMvc.perform(post("/api/v1/points/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value("G001"))
                .andExpect(jsonPath("$.error.message").value("{valid.point.positive}"));

    }
}
