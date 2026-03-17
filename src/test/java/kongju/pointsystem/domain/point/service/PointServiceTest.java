package kongju.pointsystem.domain.point.service;

import java.util.Optional;
import java.util.UUID;


import kongju.pointsystem.domain.point.dto.PointBalanceRequest;
import kongju.pointsystem.domain.point.dto.PointBalanceResponse;
import kongju.pointsystem.global.error.exception.UserNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import kongju.pointsystem.domain.point.dto.PointRequest;
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
    @DisplayName("포인트를 성공적으로 적립했을 경우")
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
    @DisplayName("0포인트를 적립했을 경우")
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
    @DisplayName("음수를 적립했을 경우")
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
    @DisplayName("id가 없을 경우")
    void should_find_id_when_not_exist(){
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        PointRequest request = new PointRequest(userId, 500L);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            pointService.earnPoint(request);
        });

        assertThat(exception.getMessage()).isEqualTo("존재하지 않는 계정입니다");
    }


    @Test
    @DisplayName("포인트가 성공적으로 조회될 경우")
    void should_balance_success_when_points_Check() {
        UUID userId = UUID.randomUUID();
        User user = UserFixture.create();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        user.getUserBalance().earn(1000L);

        PointBalanceRequest request = new PointBalanceRequest(userId, null);
        PointBalanceResponse response = (PointBalanceResponse)pointService.balancePoint(request);

        assertThat(user.getUserBalance().getTotalAmount()).isEqualTo(1000L);
        assertThat(response.balance()).isEqualTo(1000L);
    }
}
