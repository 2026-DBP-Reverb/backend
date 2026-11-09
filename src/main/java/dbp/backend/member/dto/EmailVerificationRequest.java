package dbp.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmailVerificationRequest(
        @Schema(
                description = "인증 코드를 받을 교내 이메일",
                example = "20260001@dongduk.ac.kr",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email
) {
}
