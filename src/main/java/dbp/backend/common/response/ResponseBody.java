package dbp.backend.common.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonPropertyOrder({"success", "data"})
public class ResponseBody<T> extends BaseResponse {
    @Schema(description = "성공 응답 데이터")
    private final T data;

    public ResponseBody(T data) {
        super(true);
        this.data = data;
    }

    public T getData() {
        return data;
    }
}
