package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class RefundReferenceIdNotExsistException extends BusinessException {
    public RefundReferenceIdNotExsistException() {
        super(ErrorCode.REFUND_NOT_EXSIST_REFERENCEID);
    }
}
