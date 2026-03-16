package kongju.pointsystem.global.common;

import lombok.*;

import kongju.pointsystem.global.error.ErrorCode;


@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ErrorResponse error;

    /**
     * 성공했을 때 데이터가 있는 경우 응답
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(
                true,
                data,
                null
        );
    }

    /**
     * 성공했을 때 데이터가 없는 경우 응답
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(
                true,
                null,
                null
        );
    }

    /**
     * 실패했을 때 응답
     */
    public static ApiResponse<Void> fail(ErrorCode errorCode) {
        return new ApiResponse<>(
                false,
                null,
                ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build()
        );
    }

    /**
     * 실패했을 때 (메시지를 직접 입력받는 경우)
     */
    public static ApiResponse<Void> fail(ErrorCode errorCode, String message) {
        return new ApiResponse<>(
                false,
                null,
                ErrorResponse.builder()
                        .code(errorCode.getCode())
                        .message(message)
                        .build()
        );
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ErrorResponse {
        private String code;
        private String message;
    }
}

