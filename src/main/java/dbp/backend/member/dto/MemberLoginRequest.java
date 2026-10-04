package dbp.backend.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberLoginRequest (
    @Schema(
            description = "이메일",
            example = "20230774@dongduk.ac.kr",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String email,

    @Schema(
            description = "비밀번호",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    String password
){
}
