package kongju.pointsystem.global.error.exception;

public class RefundReferenceIdNotExsistException extends BusinessException {
    public RefundReferenceIdNotExsistException() {
        super(ErrorCode.REFUND_NOT_EXSIST_REFERENCEID);
    }
}
