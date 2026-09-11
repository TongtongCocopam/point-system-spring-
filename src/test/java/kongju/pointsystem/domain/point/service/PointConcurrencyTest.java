package kongju.pointsystem.domain.point.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.*;
import java.util.stream.Stream;

import kongju.pointsystem.support.UserFixture;
import kongju.pointsystem.domain.user.entity.User;
import kongju.pointsystem.domain.point.dto.PointRequest;
import kongju.pointsystem.domain.user.entity.UserBalance;
import kongju.pointsystem.domain.point.entity.PointDetail;
import kongju.pointsystem.domain.user.repository.UserRepository;
import kongju.pointsystem.domain.user.repository.UserBalanceRepository;
import kongju.pointsystem.domain.point.repository.PointUsageRepository;
import kongju.pointsystem.domain.point.repository.PointDetailRepository;
import kongju.pointsystem.domain.point.repository.PointHistoryRepository;
import kongju.pointsystem.global.error.exception.BalanceNotEnoughException;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
public class PointConcurrencyTest {
    @Autowired
    private PointService pointService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBalanceRepository userBalanceRepository;

    @Autowired
    private PointDetailRepository pointDetailRepository;

    @Autowired
    private PointHistoryRepository pointHistoryRepository;

    @Autowired
    private PointUsageRepository pointUsageRepository;

    @Test
    @DisplayName("동시에 800포인트를 사용하면 하나의 요청만 성공한다")
    void should_allow_only_one_request_when_using_points_concurrently() throws Exception {

        User user = UserFixture.createWithBalance(1000L);
        userRepository.saveAndFlush(user);

        UserBalance balance = user.getUserBalance();
        userBalanceRepository.saveAndFlush(balance);

        PointDetail detail = createPointDetail(user, 1000L);
        pointDetailRepository.saveAndFlush(detail);

        UUID userId = user.getId();

        PointRequest request = new PointRequest(userId, 800L);

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> task = () -> {
            ready.countDown();
            start.await();

            try {
                pointService.usePoint(request);
                return true;
            } catch (BalanceNotEnoughException e) {
                return false;
            }
        };

        try {
            Future<Boolean> first = executor.submit(task);
            Future<Boolean> second = executor.submit(task);

            ready.await();
            start.countDown();

            boolean firstResult = first.get();
            boolean secondResult = second.get();

            long successCount = Stream.of(firstResult, secondResult)
                    .filter(Boolean::booleanValue)
                    .count();

            assertThat(successCount).isEqualTo(1);

            UserBalance resultBalance = userBalanceRepository.findByUserId(userId)
                    .orElseThrow();

            assertThat(resultBalance.getTotalAmount()).isEqualTo(200L);

        } finally {
            executor.shutdown();
        }
    }

    private PointDetail createPointDetail(User user, long amount) {
        return PointDetail.builder()
                .user(user)
                .amount(amount)
                .build();
    }
}
