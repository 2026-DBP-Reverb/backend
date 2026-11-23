package dbp.backend.settlement.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ConfirmTransferResponse(
        @Schema(description = "DB에 저장된 송금 확정 시각", example = "2026-11-23T14:30:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDateTime confirmedAt
) {
}
