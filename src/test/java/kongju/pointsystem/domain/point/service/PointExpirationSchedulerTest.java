package kongju.pointsystem.domain.point.service;


import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

import kongju.pointsystem.domain.point.scheduling.PointExpirationScheduler;


@ExtendWith(MockitoExtension.class)
public class PointExpirationSchedulerTest {
    @Mock
    private PointService pointService;
    @InjectMocks
    private PointExpirationScheduler pointExpirationScheduler;

    @Test
    @DisplayName("스케줄러 실행")
    void should_call_expirePoint_when_scheduler_run() {
        pointExpirationScheduler.runPointExpiration();

        verify(pointService).expirePoint();
    }
}
