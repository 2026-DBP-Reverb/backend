package dbp.backend.recommendation.dto.request;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메이트신청 요청 DTO
 * 신청자(requester)는 로그인 세션에서 가져오므로 수신자 ID만 받음
 */
public record MateRequestCreateRequest(
    @Schema(
            description = "요청 받을 사람의 회원번호",
            example = "21",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    Long recipientId
) {
}

