package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class UserNotFoundException extends BusinessException {
    public UserNotFoundException() {
        super(ErrorCode.USER_NOT_FOUND);
    }
}
