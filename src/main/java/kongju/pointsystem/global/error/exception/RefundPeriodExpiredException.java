package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class RefundPeriodExpiredException extends BusinessException{
    public RefundPeriodExpiredException() {
        super(ErrorCode.REFUND_PERIOD_EXPIRED);
    }
}
