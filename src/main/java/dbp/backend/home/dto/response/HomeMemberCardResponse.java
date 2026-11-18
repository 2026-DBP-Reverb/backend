package dbp.backend.home.dto.response;

import dbp.backend.common.util.ProfileImageUrls;
import dbp.backend.recommendation.model.MateCandidate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "홈 최근 가입 회원 카드")
public record HomeMemberCardResponse(
        @Schema(
                description = "회원 ID",
                example = "12"
        )
        Long memberId,

        @Schema(
                description = "회원 닉네임",
                example = "박솜솜"
        )
        String nickname,

        @Schema(
                description = "프로필 이미지 URL",
                example = "https://example.com/profile.png"
        )
        String profileImageUrl,

        @Schema(
                description = "로그인 회원과 공통으로 가진 음악 취향 태그",
                example = "[\"YOASOBI\", \"TWS\", \"RIIZE\"]"
        )
        List<String> hashtags,

        @Schema(
                description = "음악 취향 일치율(%)",
                example = "67"
        )
        int matchRate
) {
    public static HomeMemberCardResponse of(
            MateCandidate candidate,
            List<String> hashtags
    ){
        return new HomeMemberCardResponse(
                candidate.getMemberId(),
                candidate.getNickname(),
                ProfileImageUrls.resolve(
                        candidate.getProfileImageUrl()
                ),
                hashtags,
                candidate.getMatchRate()
        );
    }
}