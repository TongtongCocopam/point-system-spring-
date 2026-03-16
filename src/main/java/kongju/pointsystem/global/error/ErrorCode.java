package kongju.pointsystem.global.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "존재하지 않는 계정입니다"),
    BALANCE_NOT_ENOUGH(HttpStatus.BAD_REQUEST, "P001", "포인트 잔액이 부족합니다"),
    REFUND_PERIOD_EXPIRED(HttpStatus.BAD_REQUEST, "P002", "환불 기간이 만료되었습니다"),
    REFUND_ALREADY_PROCESSED(HttpStatus.BAD_REQUEST, "P003", "이미 환불 처리되었습니다"),
    REFUND_NOT_EXSIST_REFERENCEID(HttpStatus.NOT_FOUND, "P004", "존재하지 않는 영수증 번호 입니다"),
//    POINT_INVALID(HttpStatus.BAD_REQUEST, "P005", "잘못된 적립 금액입니다"),
    INTERNAL_SERVER_ERROR(HttpStatus.BAD_REQUEST, "S001", "서버 내부 오류가 발생했습니다"),
    DATABASE_CONNECTION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "D001", "서버 점검 중입니다"),
    DUPLICATE_DATA(HttpStatus.BAD_REQUEST, "D002", "이미 사용 중인 정보입니다"),
    DATA_CONFLICT(HttpStatus.INTERNAL_SERVER_ERROR, "D003", "서버 점검 중입니다"),
    UNKNOWN_DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "D004", "서버 점검 중입니다"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "P005", null);

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
