package kongju.pointsystem.domain.point.controller;

import java.time.LocalDateTime;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import kongju.pointsystem.domain.point.dto.*;
import kongju.pointsystem.domain.point.service.PointService;
import kongju.pointsystem.global.error.ErrorCode;
import kongju.pointsystem.global.error.exception.*;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        PointRequest request = new PointRequest(UUID.randomUUID(), -1000L);

        mockMvc.perform(post("/api/v1/points/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value("{valid.point.positive}"));

    }

    @Test
    @DisplayName("POST /api/v1/points/earn - 실패 : User가 존재하지 않는 경우")
    void earn_fail_not_found_user() throws Exception {
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        PointRequest request = new PointRequest(UUID.randomUUID(), 10L);

        when(pointService.earnPoint(any(PointRequest.class)))
                .thenThrow(new UserNotFoundException());

        mockMvc.perform(post("/api/v1/points/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value(errorCode.getMessage()));

    }

    @Test
    @DisplayName("GET /api/v1/points/balance - 조회 성공: 만료 예정 시간 있음")
    void balance_success() throws Exception {
        LocalDateTime time = LocalDateTime.now().plusMonths(1);
        PointBalanceExpireResponse response = PointBalanceExpireResponse.builder()
                .balance(1000L)
                .expiredAt(time)
                .build();

        when(pointService.balancePoint(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/points/balance")
                        .param("id", UUID.randomUUID().toString())
                        .param("time", time.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data['잔액']").value(1000))
                .andExpect(jsonPath("$.data['만료 예정일']").exists());
    }

    @Test
    @DisplayName("GET /api/v1/points/balance - 조회 성공 : 만료 예정 시간 없이")
    void balance_success_expriredAt() throws Exception {
        LocalDateTime time = LocalDateTime.now().plusMonths(1);
        PointBalanceResponse response = PointBalanceResponse.builder()
                .balance(1000L)
                .build();

        when(pointService.balancePoint(any())).thenReturn(response);

        mockMvc.perform(get("/api/v1/points/balance")
                        .param("id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data['잔액']").value(1000));
    }


    @Test
    @DisplayName("GET /api/v1/points/balance - 실패 : User가 존재하지 않는 경우")
    void balance_fail_not_found_user() throws Exception {
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        when(pointService.balancePoint(any()))
                .thenThrow(new UserNotFoundException());

        mockMvc.perform(get("/api/v1/points/balance")
                        .param("id", UUID.randomUUID().toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value(errorCode.getMessage()));

    }

    @Test
    @DisplayName("POST /api/v1/points/use - 사용 성공 : 잔액이 충분할 경우")
    void use_success() throws Exception {
        UUID userId = UUID.randomUUID();
        PointRequest request = new PointRequest(userId, 1000L);

        PointUseResponse response = PointUseResponse.builder()
                .useAmount(1000L)
                .currentBalance(2000L)
                .build();

        when(pointService.usePoint(any(PointRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/points/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data['사용 금액']").value(1000))
                .andExpect(jsonPath("$.data['현재 잔액']").value(2000))
                .andExpect(jsonPath("$.error").isEmpty());
    }


    @Test
    @DisplayName("POST /api/v1/points/use - 사용 실패 : 음수 잔액 차감")
    void use_fail_invalid_point() throws Exception {
        UUID userId = UUID.randomUUID();
        PointRequest request = new PointRequest(userId, -100L);

        ErrorCode errorCode = ErrorCode.INVALID_INPUT;

        mockMvc.perform(post("/api/v1/points/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value("{valid.point.positive}"));

    }


    @Test
    @DisplayName("POST /api/v1/points/use - 사용 실패 : 잔액 부족 -1")
    void use_fail_not_enough_balance() throws Exception {
        UUID userId = UUID.randomUUID();
        PointRequest request = new PointRequest(userId, 100L);

        ErrorCode errorCode = ErrorCode.BALANCE_NOT_ENOUGH;
        when(pointService.usePoint(any(PointRequest.class))).thenThrow(new BalanceNotEnoughException());
        mockMvc.perform(post("/api/v1/points/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value(errorCode.getMessage()));

    }


    @Test
    @DisplayName("POST /api/v1/points/use - 실패 : User가 존재하지 않는 경우")
    void use_fail_not_found_user() throws Exception {
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        PointRequest request = new PointRequest(UUID.randomUUID(), 10L);

        when(pointService.usePoint(any(PointRequest.class)))
                .thenThrow(new UserNotFoundException());

        mockMvc.perform(post("/api/v1/points/use")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value(errorCode.getMessage()));

    }

    @Test
    @DisplayName("POST /api/v1/points/refund - 환불 성공")
    void refund_success() throws Exception {
        RefundRequest request = new RefundRequest(UUID.randomUUID(), UUID.randomUUID());

        PointRefundResponse response = PointRefundResponse.builder()
                .refundAmount(2000L)
                .expiredAmount(200L)
                .currentBalance(3000L)
                .message("환불 완료")
                .build();

        when(pointService.refundPoint(any(RefundRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/points/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data['환불 금액']").value(2000))
                .andExpect(jsonPath("$.data['만료된 금액']").value(200))
                .andExpect(jsonPath("$.data['현재 잔액']").value(3000))
                .andExpect(jsonPath("$.data['처리 결과']").value("환불 완료"))
                .andExpect(jsonPath("$.error").isEmpty());

    }

    @Test
    @DisplayName("POST /api/v1/points/refund - 실패 : User가 존재하지 않는 경우")
    void refund_fail_not_found_user() throws Exception {
        ErrorCode errorCode = ErrorCode.USER_NOT_FOUND;

        RefundRequest request = new RefundRequest(UUID.randomUUID(), UUID.randomUUID());

        when(pointService.refundPoint(any(RefundRequest.class)))
                .thenThrow(new UserNotFoundException());

        mockMvc.perform(post("/api/v1/points/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value(errorCode.getMessage()));

    }

    @Test
    @DisplayName("POST /api/v1/points/refund - 실패 : referenceId가 유효하지 않은 경우")
    void refund_fail_invalid_referenceId() throws Exception {
        ErrorCode errorCode = ErrorCode.REFUND_NOT_EXSIST_REFERENCEID;

        RefundRequest request = new RefundRequest(UUID.randomUUID(), UUID.randomUUID());

        when(pointService.refundPoint(any(RefundRequest.class)))
                .thenThrow(new RefundReferenceIdNotExsistException());

        mockMvc.perform(post("/api/v1/points/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value(errorCode.getMessage()));

    }

    @Test
    @DisplayName("POST /api/v1/points/refund - 실패 : 환불이 이미 처리된 경우")
    void refund_fail_already_processed() throws Exception {
        ErrorCode errorCode = ErrorCode.REFUND_ALREADY_PROCESSED;

        RefundRequest request = new RefundRequest(UUID.randomUUID(), UUID.randomUUID());

        when(pointService.refundPoint(any(RefundRequest.class)))
                .thenThrow(new RefundAlreadyProcessedException());

        mockMvc.perform(post("/api/v1/points/refund")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.error.code").value(errorCode.getCode()))
                .andExpect(jsonPath("$.error.message").value(errorCode.getMessage()));

    }

}
