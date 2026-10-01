package dbp.backend.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "표준 API 응답. 성공 시 data가, 실패 시 error가 포함됩니다.")
public class ResponseBody<T> {
    private final boolean success;
    private final T data;
    private final ApiError error;

    private ResponseBody(boolean success, T data, ApiError error) {
        this.success = success;
        this.data = data;
        this.error = error;
    }

    public static <T> ResponseBody<T> success(T data) {
        return new ResponseBody<>(true, data, null);
    }

    public static ResponseBody<Void> error(ApiError error) {
        return new ResponseBody<>(false, null, error);
    }

    @Schema(description = "요청 성공 여부", example = "true")
    public boolean isSuccess() {
        return success;
    }

    @Schema(description = "성공 응답 데이터. 실패 시에는 포함되지 않습니다.")
    public T getData() {
        return data;
    }

    @Schema(description = "오류 정보. 성공 시에는 포함되지 않습니다.")
    public ApiError getError() {
        return error;
    }
}
