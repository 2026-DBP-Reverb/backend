package dbp.backend.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import dbp.backend.common.exception.code.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({"success", "error"})
public class ApiError extends BaseResponse {
    @Schema(description = "오류 정보")
    private final ErrorDetail error;

    public ApiError(ErrorCode errorCode) {
        super(false);
        this.error = new ErrorDetail(
                resolveCode(errorCode),
                errorCode.getDetail()
        );
    }

    public ErrorDetail getError() {
        return error;
    }

    @Schema(description = "표준 API 오류 정보")
    public record ErrorDetail(
            @Schema(description = "에러 코드", example = "BAD_REQUEST")
            String code,
            @Schema(description = "에러 설명", example = "잘못된 요청입니다.")
            String detail
    ) {
    }

    private static String resolveCode(ErrorCode errorCode) {
        if (errorCode instanceof Enum<?> enumValue) {
            return enumValue.name();
        }
        return errorCode.getClass().getSimpleName();
    }
}
