package kongju.pointsystem.global.error.exception;

public class EmailDuplicatedException extends BusinessException {
    public EmailDuplicatedException() {
      super(ErrorCode.DUPLICATE_DATA);
    }
}
