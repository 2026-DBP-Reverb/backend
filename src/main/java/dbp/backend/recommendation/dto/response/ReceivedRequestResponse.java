package dbp.backend.recommendation.dto.response;

import dbp.backend.recommendation.model.ReceivedRequest;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

//(Long requestId, Long requesterId, String nickname, String profileImageUrl, LocalDateTime requestedAt)

public record ReceivedRequestResponse(
    @Schema(
            description = "메이트신청id",
            example = "100"
    )
    Long requestId,

    @Schema(
            description = "신청자 id",
            example = "50"
    )
    Long requesterId,

    @Schema(
            description = "신청자 닉네임",
            example = "somsom28"
    )
    String nickname,

    @Schema(
            description = "프로필이미지 url",
            example = "https://image.com/61"
    )
    String profileImageUrl,

    @Schema(
            description = "메이트 신청 시각",
            example = "2026-11-02T16:34:30.388"
    )
    LocalDateTime requestedAt
) {
    private static final String IMAGE_SERVER_URI = "http://localhost:8080";
    public static ReceivedRequestResponse from(ReceivedRequest request){
        return new ReceivedRequestResponse(request.requestId(), request.requesterId(), request.nickname(), IMAGE_SERVER_URI + request.profileImageUrl(),
                request.requestedAt());
    }
}
