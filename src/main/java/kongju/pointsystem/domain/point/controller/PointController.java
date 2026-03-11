package kongju.pointsystem.domain.point.controller;

import java.util.UUID;

import kongju.pointsystem.global.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import kongju.pointsystem.domain.point.dto.PointRequest;
import kongju.pointsystem.domain.point.dto.RefundRequest;


@RestController
@RequestMapping("/api/v1/points")
@RequiredArgsConstructor
public class PointController {

    @PostMapping("/earn")
    public ResponseEntity<ApiResponse<Void>> EarnPoint(@RequestBody PointRequest request) {
        return null;
    }

    @PostMapping("/use")
    public ResponseEntity<ApiResponse<String>> usePoint(@RequestBody PointRequest request) {
        return null;
    }

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<String>> balancePoint(@PathVariable UUID id) {
        return null;
    }

    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<String>> refundPoint(@RequestBody RefundRequest request) {
        return null;
    }

}
