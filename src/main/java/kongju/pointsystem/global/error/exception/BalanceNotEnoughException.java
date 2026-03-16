package kongju.pointsystem.global.error.exception;

import kongju.pointsystem.global.error.ErrorCode;

public class BalanceNotEnoughException extends BusinessException{
    public BalanceNotEnoughException() {
        super(ErrorCode.BALANCE_NOT_ENOUGH);
    }
}
