package kongju.pointsystem.global.error.exception;

public class RefundPeriodExpiredException extends BusinessException{
    public RefundPeriodExpiredException() {
        super(ErrorCode.REFUND_PERIOD_EXPIRED);
    }
}
