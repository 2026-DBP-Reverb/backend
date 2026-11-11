package dbp.backend.meetup.dto.response;

import dbp.backend.meetup.model.MeetupCategory;
import dbp.backend.meetup.model.MeetupStatus;
import dbp.backend.meetup.model.MeetupSummary;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "모임 목록 항목")
public record MeetupListResponse(
        @Schema(description = "모임 ID", example = "1")
        Long meetupId,

        @Schema(
                description = "모임 제목",
                example = "부산락페 동행해요"
        )
        String title,

        @Schema(
                description = "모임 이미지 URL",
                example = "https://example.com/meeting.png"
        )
        String imageUrl,

        @Schema(
                description = "모임 장소",
                example = "부산광역시"
        )
        String place,

        @Schema(
                description = "모임 카테고리",
                example = "FESTIVAL"
        )
        MeetupCategory category,

        @Schema(
                description = "현재 참여 인원",
                example = "2"
        )
        Integer participantCount,

        @Schema(
                description = "최대 참여 인원",
                example = "4"
        )
        Integer capacity,

        @Schema(
                description = "모임 시작 일시",
                example = "2026-11-20T12:00:00"
        )
        LocalDateTime startsAt,

        @Schema(
                description = "모임 상태",
                example = "OPEN"
        )
        MeetupStatus status,

        @Schema(
                description = "로그인 회원의 참여 여부",
                example = "true"
        )
        boolean joined
) {
        public static MeetupListResponse from(
                MeetupSummary summary
        ) {
                return new MeetupListResponse(
                        summary.meetupId(),
                        summary.title(),
                        summary.imageUrl(),
                        summary.place(),
                        summary.category(),
                        summary.participantCount(),
                        summary.capacity(),
                        summary.startsAt(),
                        summary.status(),
                        summary.joined()
                );
        }
}