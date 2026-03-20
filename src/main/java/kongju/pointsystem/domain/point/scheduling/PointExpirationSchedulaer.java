package kongju.pointsystem.domain.point.scheduling;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import kongju.pointsystem.domain.point.service.PointService;

@Component
@RequiredArgsConstructor
public class PointExpirationSchedulaer {
    private final PointService pointService;

    @Scheduled(cron = "0 0 0 * * *")
    public void runPointExpiration() {
        pointService.expirePoint();
    }
}
