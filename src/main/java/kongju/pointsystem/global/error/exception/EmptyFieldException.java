package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class EmptyFieldException extends BusinessException {
    public EmptyFieldException() {
      super(ErrorCode.EMPTY_FIELD);
    }
}
