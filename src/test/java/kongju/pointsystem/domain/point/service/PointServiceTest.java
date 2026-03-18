package kongju.pointsystem.domain.point.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import kongju.pointsystem.domain.point.dto.*;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.global.error.exception.BalanceNotEnoughException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.support.UserFixture;
import kongju.pointsystem.domain.point.repository.PointDetailRepository;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.domain.point.entity.PointDetail;
import kongju.pointsystem.domain.point.entity.PointHistory;
import kongju.pointsystem.domain.point.repository.PointHistoryRepository;
import kongju.pointsystem.domain.point.repository.PointUsageRepository;
import kongju.pointsystem.domain.user.repository.UserBalanceRepository;
import kongju.pointsystem.global.error.exception.InvalidPointAmountException;
import kongju.pointsystem.global.error.exception.UserNotFoundException;


@ExtendWith(MockitoExtension.class)
public class PointServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PointDetailRepository pointDetailRepository;
    @Mock
    private PointHistoryRepository pointHistoryRepository;
    @Mock
    private PointUsageRepository pointUsageRepository;
    @Mock
    private UserBalanceRepository userBalanceRepository;

    @InjectMocks
    private PointService pointService;

    @Test
    @DisplayName("earn : 포인트를 성공적으로 적립했을 경우")
    void should_earn_sucess_when_points_are_earned() {

        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        PointRequest request = new PointRequest(userId, 1000L);

        var response = pointService.earnPoint(request);

        // 포인트 디테일 생성
        verify(pointDetailRepository, times(1)).save(any(PointDetail.class));

        // 포인트 히스토리 생성
        verify(pointHistoryRepository, times(1)).save(any(PointHistory.class));

        // 유저 잔액 변경
        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1000L);
        assertThat(response.currentBalance().equals(1000L));
    }

    @Test
    @DisplayName("earn : 0포인트를 적립했을 경우")
    void should_zero_point_earn_when_points_are_not_earned() {
        UUID userId = UUID.randomUUID();

        PointRequest request = new PointRequest(userId, 0L);

        InvalidPointAmountException exception = assertThrows(InvalidPointAmountException.class, () -> {
            pointService.earnPoint(request);
        });

        // 에러 메시지 확인
        assertThat(exception.getMessage()).isEqualTo("잘못된 적립 금액입니다");
    }

    @Test
    @DisplayName("earn : 음수를 적립했을 경우")
    void should_negative_point_earn_when_points_are_not_earned() {
        UUID userId = UUID.randomUUID();

        PointRequest request = new PointRequest(userId, -500L);

        InvalidPointAmountException exception = assertThrows(InvalidPointAmountException.class, () -> {
            pointService.earnPoint(request);
        });

        // 에러 메시지 확인
        assertThat(exception.getMessage()).isEqualTo("잘못된 적립 금액입니다");
    }

    @Test
    @DisplayName("earn : User가 존재하지 않는 경우")
    void should_find_id_when_not_exist() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        PointRequest request = new PointRequest(userId, 500L);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            pointService.earnPoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 계정입니다");
    }


    @Test
    @DisplayName("balance : 포인트가 성공적으로 조회될 경우")
    void should_balance_success_when_points_Check() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        user.getUserBalance().earn(1000L);

        PointBalanceRequest request = new PointBalanceRequest(userId, null);
        PointBalanceResponse response = (PointBalanceResponse) pointService.balancePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1000L);
        assertThat(response.balance()).isEqualTo(1000L);
    }

    @Test
    @DisplayName("balance : 만료 예정 시간을 넣은 경우")
    void should_null_expiredAt_when_points_Check() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();

        PointDetail detail1 = PointDetail.builder()
                .amount(1000L)
                .user(user)
                .build();
        PointDetail detail2 = PointDetail.builder()
                .amount(1500L)
                .user(user)
                .build();

        LocalDateTime future = LocalDateTime.now().plusMonths(1);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        when(pointDetailRepository.findByPointExpire(eq(userId), eq(future)))
                .thenReturn(List.of(detail1, detail2));

        PointBalanceRequest request = new PointBalanceRequest(userId, future);

        PointBalanceExpireResponse response = (PointBalanceExpireResponse) pointService.balancePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(2500L);
        assertThat(response.expiredAt()).isEqualTo(future);
    }

    @Test
    @DisplayName("balance : 포인트가 없는경우")
    void should_return_zero_point_when_points_not_exist() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        PointBalanceRequest request = new PointBalanceRequest(userId, null);

        PointBalanceResponse response = (PointBalanceResponse) pointService.balancePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(0L);
        assertThat(response.balance()).isEqualTo(0L);
    }

    @Test
    @DisplayName("balance : User가 존재하지 않는 경우")
    void should_throw_UserNotFoundException_when_balance_user_not_found() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        PointBalanceRequest request = new PointBalanceRequest(userId, null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            pointService.balancePoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 계정입니다");
    }

    @Test
    @DisplayName("balance : 만료 예정 포인트를 확인할 날짜가 현재 날짜 이전인 경우")
    void should_return_zero_when_check_date_is_in_the_past() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        LocalDateTime past = LocalDateTime.now().minusDays(1);
        PointBalanceRequest request = new PointBalanceRequest(userId, past);

        PointBalanceExpireResponse response = (PointBalanceExpireResponse) pointService.balancePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("use : 잔액이 1원 더 많은 경우")
    void should_use_success_when_balance_enough() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createWithBalance(2500L);

        PointDetail pointDetail1 = PointDetail.builder()
                .user(user)
                .amount(1000)
                .build();
        PointDetail pointDetail2 = PointDetail.builder()
                .user(user)
                .amount(1500)
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(pointDetailRepository.findRemainedDetailsNotExpired(eq(userId), any())).thenReturn(List.of(pointDetail1, pointDetail2));

        PointRequest request = new PointRequest(userId, 2499L);
        PointUseResponse response = pointService.usePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1L);
        assertThat(pointDetail1.getRemainAmount()).isEqualTo(0L);
        assertThat(pointDetail2.getRemainAmount()).isEqualTo(1L);
        assertThat(response.currentBalance()).isEqualTo(1L);
    }

    @Test
    @DisplayName("use : 잔액이 부족한 경우 ")
    void should_throw_Balance_Not_Enough_exception_when_balance_not_enough() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createWithBalance(1000L);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        PointRequest request = new PointRequest(userId, 1001L);
        BalanceNotEnoughException exception = assertThrows(BalanceNotEnoughException.class, () -> {
            pointService.usePoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("포인트 잔액이 부족합니다");
    }

    @Test
    @DisplayName("use : 잔액과 사용 금액이 일치하는 경우")
    void should_sucess_use_point_when_balance_enough() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.createWithBalance(2500L);
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        PointDetail pointDetail1 = PointDetail.builder()
                .user(user)
                .amount(1000)
                .build();
        PointDetail pointDetail2 = PointDetail.builder()
                .user(user)
                .amount(1500)
                .build();

        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(pointDetailRepository.findRemainedDetailsNotExpired(eq(userId), any())).thenReturn(List.of(pointDetail1, pointDetail2));

        PointRequest request = new PointRequest(userId, 2500L);
        PointUseResponse response = pointService.usePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(0L);
        assertThat(pointDetail1.getRemainAmount()).isEqualTo(0L);
        assertThat(pointDetail2.getRemainAmount()).isEqualTo(0L);
        assertThat(response.currentBalance()).isEqualTo(0L);

    }

}
