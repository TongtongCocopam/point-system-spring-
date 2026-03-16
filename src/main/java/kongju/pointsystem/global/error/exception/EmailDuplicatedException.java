package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class EmailDuplicatedException extends BusinessException {
    public EmailDuplicatedException() {
      super(ErrorCode.DUPLICATE_DATA);
    }
}
