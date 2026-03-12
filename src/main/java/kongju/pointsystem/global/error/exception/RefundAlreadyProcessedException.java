package kongju.pointsystem.global.error.exception;

public class RefundAlreadyProcessedException extends BusinessException{

    public RefundAlreadyProcessedException() {
        super(ErrorCode.REFUND_ALREADY_PROCESSED);
    }
}
