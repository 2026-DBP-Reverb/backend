package dbp.backend.recommendation.dto.response;

import dbp.backend.recommendation.model.MateCandidate;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MateRecommendationResponse(
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
                description = "취향 일치율(%) - 자카드 유사도",
                example = "25"
        )
        int matchRate,

        @Schema(description = "공통 아티스트",
                example = "[\"sakanaction\"]")
        List<String> commonArtists,

        @Schema(description = "공통 장르",
                example = "[\"KPOP\"]")
        List<String> commonGenres,

        @Schema(description = "공통 곡",
                example = "[]")
        List<String> commonTracks
) {
    private static final String IMAGE_SERVER_URI = "http://localhost:8080";

    public static MateRecommendationResponse of(
            MateCandidate mateCandidate,
            List<String> commonArtists,
            List<String> commonGenres,
            List<String> commonTracks
    ){
        return new MateRecommendationResponse(
                mateCandidate.getMemberId(),
                mateCandidate.getNickname(),
                IMAGE_SERVER_URI + mateCandidate.getProfileImageUrl(),
                mateCandidate.getMatchRate(),
                commonArtists,
                commonGenres,
                commonTracks
        );
    }
}
