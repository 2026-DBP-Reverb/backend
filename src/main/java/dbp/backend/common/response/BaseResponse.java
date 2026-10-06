package dbp.backend.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

public class BaseResponse {
    @Schema(description = "요청 성공 여부", example = "true")
    private final boolean success;

    BaseResponse(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }
}
