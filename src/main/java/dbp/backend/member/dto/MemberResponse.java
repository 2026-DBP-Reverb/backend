package dbp.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record MemberResponse (
        @Schema(
                description = "회원 ID",
                example = "1"
        )
        Long memberId,

        @Schema(
                description = "회원 이메일",
                example = "student@dongduk.ac.kr"
        )
        String email,

        @Schema(
                description = "가입 일시",
                example = "2026-10-03T19:30:00"
        )
        LocalDateTime createdAt
){
}
