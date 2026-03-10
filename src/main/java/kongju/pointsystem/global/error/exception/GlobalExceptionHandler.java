package kongju.pointsystem.global.error.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.UncategorizedDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import kongju.pointsystem.global.common.ApiResponse;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 커스텀 클래스 에러 처리 핸들러
     * @param e
     * @return
     */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("BusinessException : {}", e.getMessage());
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ApiResponse.fail(errorCode));
    }

    /**
     * 예상치 못한 에러 처리 핸들러
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Exception : ", e);
        return ResponseEntity
                .status(ErrorCode.INTERNAL_SERVER_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.INTERNAL_SERVER_ERROR));

    }

    /**
     * DB 연결 끊김/타임아웃
     * @param e
     * @return
     */
    @ExceptionHandler(DataAccessResourceFailureException.class)
    protected ResponseEntity<ApiResponse<Void>> handleDataAccessResourceFailureException(DataAccessResourceFailureException e) {
        log.error("Exception : ", e);
        return ResponseEntity
                .status(ErrorCode.DATABASE_CONNECTION_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.DATABASE_CONNECTION_ERROR));

    }

    /**
     * DB 제약 조건 위반
     * @param e
     * @return
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<ApiResponse<Void>> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.error("Exception : ", e);
        return ResponseEntity
                .status(ErrorCode.DUPLICATE_DATA.getStatus())
                .body(ApiResponse.fail(ErrorCode.DUPLICATE_DATA));

    }

    /**
     * DB 데이터 충돌
     * @param e
     * @return
     */
    @ExceptionHandler(OptimisticLockingFailureException.class)
    protected ResponseEntity<ApiResponse<Void>> handleOptimisticLockingFailureException(OptimisticLockingFailureException e) {
        log.error("Exception : ", e);
        return ResponseEntity
                .status(ErrorCode.DATA_CONFLICT.getStatus())
                .body(ApiResponse.fail(ErrorCode.DATA_CONFLICT));

    }

    /**
     * DB알 수 없는 오류
     * @param e
     * @return
     */
    @ExceptionHandler(UncategorizedDataAccessException.class)
    protected ResponseEntity<ApiResponse<Void>> handleUncategorizedDataAccessException(UncategorizedDataAccessException e) {
        log.error("Exception : ", e);
        return ResponseEntity
                .status(ErrorCode.UNKNOWN_DATABASE_ERROR.getStatus())
                .body(ApiResponse.fail(ErrorCode.UNKNOWN_DATABASE_ERROR));

    }
}
