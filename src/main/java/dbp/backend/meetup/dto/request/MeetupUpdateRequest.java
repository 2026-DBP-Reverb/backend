package dbp.backend.meetup.dto.request;

import dbp.backend.meetup.model.MeetupCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "모임 정보 수정 요청")
public record MeetupUpdateRequest(
        @Schema(
                description = "모임 제목",
                example = "부산락페 동행 모집",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String title,

        @Schema(
                description = "모임 설명",
                example = "부산락페 같이 가실 분을 모집합니다."
        )
        String description,

        @Schema(
                description = "모임 이미지 URL",
                example = "/images/meetups/example.jpg"
        )
        String imageUrl,

        @Schema(
                description = "모임 카테고리",
                example = "FESTIVAL",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        MeetupCategory category,

        @Schema(
                description = "모임 장소",
                example = "부산광역시 사상구 삼락생태공원",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String place,

        @Schema(
                description = "모임 정원",
                example = "8",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Integer capacity,

        @Schema(
                description = "모임 시작 일시",
                example = "2026-12-21T13:00:00",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        LocalDateTime startsAt
) {
}
