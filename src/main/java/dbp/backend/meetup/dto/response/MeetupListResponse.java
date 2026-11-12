package dbp.backend.meetup.dto.response;

import dbp.backend.meetup.model.MeetupCategory;
import dbp.backend.meetup.model.MeetupStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                description = "목록용 지역명. 서울은 구, 그 외 지역은 시·도 단위",
                example = "광진구"
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
    private static final Pattern SEOUL_DISTRICT_PATTERN =
            Pattern.compile(
                    "^서울(?:특별)?시\\s*([가-힣]{1,4}구)"
            );

    public MeetupListResponse {
        place = toRegion(place);
    }

    private static String toRegion(String place) {
        if (place == null || place.isBlank()) {
            return place;
        }

        String trimmedPlace = place.trim();
        Matcher matcher =
                SEOUL_DISTRICT_PATTERN.matcher(trimmedPlace);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return trimmedPlace.split("\\s+")[0];
    }
}
