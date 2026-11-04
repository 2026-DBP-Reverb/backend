package dbp.backend.recommendation.dto.response;

import dbp.backend.recommendation.model.MyMate;
import io.swagger.v3.oas.annotations.media.Schema;

/** 나의 메이트 목록 한 줄 응답 */
public record MyMateResponse(
        @Schema(
                description = "관계 상태(MATCHED: 매칭 완료, SENT: 내가 보낸 요청 대기 중, RECEIVED: 받은 요청)",
                example = "SENT"
        )
        String status,

        @Schema(
                description = "메이트신청id",
                example = "100"
        )
        Long requestId,

        @Schema(
                description = "상대 회원 id",
                example = "50"
        )
        Long memberId,

        @Schema(
                description = "상대 회원 닉네임",
                example = "somsom28"
        )
        String nickname,

        @Schema(
                description = "상대 회원 프로필이미지 url",
                example = "https://image.com/61"
        )
        String profileImageUrl,

        @Schema(
                description = "상대 회원 인스타그램 id(MATCHED일 때만 값이 있고, 나머지는 null)",
                example = "somsom28"
        )
        String instagramId

) {
    private static final String IMAGE_SERVER_URI = "http://localhost:8080";

    public static MyMateResponse from(MyMate myMate) {
        return new MyMateResponse(
                myMate.status(),
                myMate.requestId(),
                myMate.memberId(),
                myMate.nickname(),
                IMAGE_SERVER_URI + myMate.profileImageUrl(), myMate.instagramId()
        );
    }
}