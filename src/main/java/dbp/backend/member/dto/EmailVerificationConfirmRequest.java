package dbp.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record EmailVerificationConfirmRequest(
        @Schema(
                description = "인증 코드를 받은 교내 이메일",
                example = "20260001@dongduk.ac.kr",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String email,

        @Schema(
                description = "메일로 받은 6자리 인증 코드",
                example = "012345",
                pattern = "^[0-9]{6}$",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String verificationCode
) {
}
