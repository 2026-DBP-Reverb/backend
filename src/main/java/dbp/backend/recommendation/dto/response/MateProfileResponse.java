package dbp.backend.recommendation.dto.response;

import dbp.backend.musictaste.dto.response.ArtistResponse;
import dbp.backend.musictaste.dto.response.GenreResponse;
import dbp.backend.musictaste.dto.response.TrackResponse;
import dbp.backend.recommendation.model.RequestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MateProfileResponse(
        @Schema(
                description = "회원 ID",
                example = "1"
        )
        Long memberId,

        @Schema(
                description = "닉네임",
                example = "김솜솜"
        )
        String nickname,

        @Schema(
                description = "프로필 이미지 URL",
                example = "https://example.com/profile.png"
        )
        String profileImageUrl,

        @Schema(
                description = "인스타그램 아이디 (매칭된 상대만 공개, 아니면 null)",
                example = "somsom77"
        )
        String instagramId,

        @Schema(
                description = "최애 아티스트 목록"
        )
        List<ArtistResponse> artists,

        @Schema(
                description = "선호 장르 목록"
        )
        List<GenreResponse> genres,

        @Schema(
                description = "플레이리스트"
        )
        List<TrackResponse> tracks,

        @Schema(
                description = "메이트 요청 상태 (NONE: 신청 가능, SENT: 수락 대기중, RECEIVED: 상대가 먼저 신청함)",
                example = "NONE"
        )
        RequestStatus requestStatus
) {

}
