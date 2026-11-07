package dbp.backend.meetup.dto.response;

import dbp.backend.meetup.model.MeetupParticipant;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모임 참여 회원 정보")
public record MeetupParticipantResponse(
        @Schema(
                description = "참여 회원 ID",
                example = "10"
        )
        Long memberId,

        @Schema(
                description = "참여 회원 닉네임",
                example = "솜솜"
        )
        String nickname,

        @Schema(
                description = "참여 회원 프로필 이미지 URL",
                example = "/images/default-profile.png"
        )
        String profileImageUrl
){
    public static MeetupParticipantResponse from(
            MeetupParticipant participant
    ){
        return new MeetupParticipantResponse(
            participant.getMemberId(),
            participant.getNickname(),
            participant.getProfileImageUrl()
        );
    }
}
