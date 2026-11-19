package dbp.backend.home.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "홈 최근 등록 음악 모임 카드")
public record HomeMeetupCardResponse(
        @Schema(
                description = "모임 ID",
                example = "5"
        )
        Long meetupId,

        @Schema(
                description = "모임 이름",
                example = "부산락페 동행"
        )
        String title,

        @Schema(
                description = "현재 참여 인원",
                example = "2"
        )
        Integer participantCount,

        @Schema(
                description = "모임 최대 정원",
                example = "5"
        )
        Integer capacity,

        @Schema(
                description = "모임 이미지 URL",
                example = "https://example.com/meeting.png"
        )
        String imageUrl
) {
}