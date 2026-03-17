package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class InvalidPointAmountException extends BusinessException {
    public InvalidPointAmountException() {
        super(ErrorCode.POINT_INVALID);
    }
}
