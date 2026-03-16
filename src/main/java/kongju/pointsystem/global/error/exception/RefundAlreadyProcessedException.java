package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class RefundAlreadyProcessedException extends BusinessException{

    public RefundAlreadyProcessedException() {
        super(ErrorCode.REFUND_ALREADY_PROCESSED);
    }
}
