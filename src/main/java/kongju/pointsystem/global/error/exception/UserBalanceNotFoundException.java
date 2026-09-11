package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class UserBalanceNotFoundException extends BusinessException{
    public UserBalanceNotFoundException() {
        super(ErrorCode.USER_BALANCE_NOT_FOUND);
    }
}
