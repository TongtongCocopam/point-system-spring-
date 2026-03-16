package kongju.pointsystem.domain.point.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import kongju.pointsystem.domain.point.dto.*;
import kongju.pointsystem.domain.point.service.PointService;
import kongju.pointsystem.global.common.ApiResponse;


@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    private final PointService pointService;

    @PostMapping("/earn")
    public ResponseEntity<ApiResponse<PointEarnResponse>> earnPoint(@RequestBody PointRequest request) {
        PointEarnResponse pointEarnResponse = pointService.earnPoint(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(pointEarnResponse));
    }

    @PostMapping("/use")
    public ResponseEntity<ApiResponse<PointUseResponse>> usePoint(@RequestBody PointRequest request) {
        PointUseResponse pointUseResponse = pointService.usePoint(request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(pointUseResponse));
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<PointResponse>> balancePoint(@ModelAttribute PointBalanceRequest request) {
        PointResponse pointResponse = pointService.balancePoint(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(pointResponse));
    }

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<PointRefundResponse>> refundPoint(@RequestBody RefundRequest request) {
        PointRefundResponse pointRefundResponse = pointService.refundPoint(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(pointRefundResponse));
    }

}
