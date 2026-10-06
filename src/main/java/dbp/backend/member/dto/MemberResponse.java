package dbp.backend.member.dto;

import dbp.backend.member.model.Member;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MemberResponse(
        @Schema(
                description = "회원 ID",
                example = "1"
        )
        Long memberId,

        @Schema(
                description = "회원 이메일",
                example = "20230774@dongduk.ac.kr"
        )
        String email,

        @Schema(
                description = "프로필 이미지 URL",
                example = "https://example.com/profile.png"
        )
        String profileImageUrl,

        @Schema(
                description = "인스타그램 아이디",
                example = "reverb_music"
        )
        String instagramId,

        @Schema(
                description = "닉네임",
                example = "박솜솜"
        )
        String nickname,

        @Schema(
                description = "가입 일시",
                example = "2026-10-03T19:30:00"
        )
        LocalDateTime createdAt
) {
    private static final String IMAGE_SERVER_URI = "http://localhost:8080";

    public static MemberResponse from(Member member) {
        return new MemberResponse(
                member.getMemberId(),
                member.getEmail(),
                IMAGE_SERVER_URI + member.getProfileImageUrl(),
                member.getInstagramId(),
                member.getNickname(),
                member.getCreatedAt()
        );
    }
}
