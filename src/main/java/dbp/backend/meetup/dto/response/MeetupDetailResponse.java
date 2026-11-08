package dbp.backend.meetup.dto.response;


import dbp.backend.meetup.model.Meetup;
import dbp.backend.meetup.model.MeetupCategory;
import dbp.backend.meetup.model.MeetupParticipant;
import dbp.backend.meetup.model.MeetupStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "모임 상세 정보 응답")
public record MeetupDetailResponse(
        @Schema(
                description = "모임 ID",
                example = "1"
        )
        Long meetupId,

        @Schema(
                description = "모임장 회원 ID",
                example = "10"
        )
        Long hostMemberId,

        @Schema(
                description = "모임 제목",
                example = "부산락페 동행"
        )
        String title,

        @Schema(
                description = "모임 설명",
                example = "부산락페 같이 가실 분 구합니다."
        )
        String description,

        @Schema(
                description = "모임 이미지 URL",
                example = "https://example.com/images/meetups/example.jpg"
        )
        String imageUrl,

        @Schema(
                description = "모임 카테고리",
                example = "FESTIVAL"
        )
        MeetupCategory category,

        @Schema(
                description = "모임 장소",
                example = "부산광역시 사상구 삼락생태공원"
        )
        String place,

        @Schema(
                description = "모임 정원",
                example = "5"
        )
        Integer capacity,

        @Schema(
                description = "현재 참여 인원",
                example = "2"
        )
        Integer participantCount,

        @Schema(
                description = "모임 상태",
                example = "OPEN"
        )
        MeetupStatus status,

        @Schema(
                description = "모임 시작 일시",
                example = "2026-12-20T12:00:00"
        )
        LocalDateTime startsAt,

        @Schema(
                description = "모임 생성 일시",
                example = "2026-11-08T10:00:00"
        )
        LocalDateTime createdAt,

        @Schema(
                description = "로그인 회원의 모임장 여부",
                example = "true"
        )
        boolean host,

        @Schema(
                description = "로그인 회원의 현재 참여 여부",
                example = "true"
        )
        boolean joined,

        @Schema(description = "현재 참여 중인 회원 목록")
        List<MeetupParticipantResponse> participants
) {
    public static MeetupDetailResponse from(
            Meetup meetup,
            List<MeetupParticipant> participants,
            Long loginMemberId
    ){
        List<MeetupParticipantResponse> participantResponses = new ArrayList<>();

        boolean joined = false;

        for (MeetupParticipant participant : participants) {
            participantResponses.add(
                    MeetupParticipantResponse.from(participant)
            );

            if (participant.getMemberId().equals(loginMemberId)) {
                joined = true;
            }
        }

        boolean host = meetup.getHostMemberId().equals(loginMemberId);

        return new MeetupDetailResponse(
                meetup.getMeetupId(),
                meetup.getHostMemberId(),
                meetup.getTitle(),
                meetup.getDescription(),
                meetup.getImageUrl(),
                meetup.getCategory(),
                meetup.getPlace(),
                meetup.getCapacity(),
                participants.size(),
                meetup.getStatus(),
                meetup.getStartsAt(),
                meetup.getCreatedAt(),
                host,
                joined,
                participantResponses
        );
    }
}
