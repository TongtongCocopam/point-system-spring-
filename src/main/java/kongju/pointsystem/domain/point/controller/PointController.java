package kongju.pointsystem.domain.point.controller;

import java.util.UUID;

import kongju.pointsystem.domain.point.dto.*;
import kongju.pointsystem.domain.point.service.PointService;
import kongju.pointsystem.global.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {
    private final PointService pointService;


    @PostMapping("/earn")
    public ResponseEntity<ApiResponse<PointEarnResponse>> earnPoint(@RequestBody PointRequest request) {
        PointEarnResponse pointEarnResponse = pointService.earnPoint(
                request.id(),
                request.point()
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(pointEarnResponse));
    }

    @PostMapping("/use")
    public ResponseEntity<ApiResponse<PointUseResponse>> usePoint(@RequestBody PointRequest request) {
        PointUseResponse pointUseResponse = pointService.usePoint(
                request.id(),
                request.point()
        );
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(pointUseResponse));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<PointResponse>> balancePoint(@ModelAttribute PointBalanceRequest request) {
        PointResponse pointResponse = pointService.balancePoint(
                request.id(),
                request.time()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(pointResponse));
    }

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<PointRefundResponse>> refundPoint(@RequestBody RefundRequest request) {
        PointRefundResponse pointRefundResponse = pointService.refundPoint(
                request.id(),
                request.referenceId()
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(pointRefundResponse));
    }

}
