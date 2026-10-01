package dbp.backend.common.response;

import dbp.backend.common.exception.code.ErrorCode;

public record ApiError(String code, String detail) {
    public ApiError(ErrorCode errorCode) {
        this(resolveCode(errorCode), errorCode.getDetail());
    }

    private static String resolveCode(ErrorCode errorCode) {
        if (errorCode instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return errorCode.getClass().getSimpleName();
    }
}
