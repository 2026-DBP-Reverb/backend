package dbp.backend.common.response;

import dbp.backend.common.exception.code.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "표준 API 오류 정보")
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
